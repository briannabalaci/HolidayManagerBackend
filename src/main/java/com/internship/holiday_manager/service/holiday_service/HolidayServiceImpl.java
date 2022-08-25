package com.internship.holiday_manager.service.holiday_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.holiday.HolidayTypeAndUserName;
import com.internship.holiday_manager.dto.holiday.UpdateDetailsHolidayDto;
import com.internship.holiday_manager.dto.notification.NotificationDto;
import com.internship.holiday_manager.dto.user.UserWithTeamIdDto;
import com.internship.holiday_manager.entity.*;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import com.internship.holiday_manager.entity.enums.NotificationType;
import com.internship.holiday_manager.entity.enums.UserType;
import com.internship.holiday_manager.mapper.HolidayMapper;
import com.internship.holiday_manager.mapper.UserMapper;
import com.internship.holiday_manager.mapper.UserWithTeamIdMapper;
import com.internship.holiday_manager.repository.*;
import com.internship.holiday_manager.service.notification_service.NotificationService;
import com.internship.holiday_manager.service.substitute.SubstituteService;
import com.internship.holiday_manager.service.teamlead_service.TeamLeadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

@Service
@Slf4j
public class HolidayServiceImpl implements HolidayService{

    private final UserRepository userRepository;
    private final HolidayRepository holidayRepository;
    private final TeamRepository teamRepository;
    private final SubstituteRepository substituteRepository;
    private final NotificationService notificationService;
    private final TeamLeadService teamLeadService;
    private final SubstituteService substituteService;
    private final HolidayMapper holidayMapper;
    private final UserMapper userMapper;
    private final UserWithTeamIdMapper userWithTeamIdDtoMapper;

    private final DetailedHolidayRepository detailedHolidayRepository;
    public HolidayServiceImpl(TeamLeadService teamLeadService, UserRepository userRepository, HolidayRepository holidayRepository, TeamRepository teamRepository, NotificationService notificationService, HolidayMapper holidayMapper, SubstituteRepository substituteRepository, SubstituteService substituteService, UserMapper userMapper, UserWithTeamIdMapper userWithTeamIdDto, DetailedHolidayRepository detailedHolidayRepository) {

        this.userRepository = userRepository;
        this.holidayRepository = holidayRepository;
        this.teamRepository = teamRepository;
        this.notificationService = notificationService;
        this.teamLeadService = teamLeadService;
        this.holidayMapper = holidayMapper;
        this.substituteRepository = substituteRepository;
        this.substituteService = substituteService;
        this.userMapper = userMapper;
        this.userWithTeamIdDtoMapper = userWithTeamIdDto;
        this.detailedHolidayRepository = detailedHolidayRepository;
    }
    public HolidayDto setStatusHoliday(HolidayDto holidayDto) {
        Long userId = holidayDto.getUser().getId();
        if(this.userRepository.getById(userId).getType().equals(UserType.TEAMLEAD)){
            holidayDto.setStatus(HolidayStatus.APPROVED);
        }
        else{
            holidayDto.setStatus(HolidayStatus.PENDING);
        }
        return holidayDto;
    }
    private boolean isTeamLeadInHoliday(User teamlead){
        List<Holiday> requests = this.holidayRepository.findByUserId(teamlead.getId());

        LocalDateTime dateTime = now();

        List<Holiday> holidays = requests.stream()
                .filter(h -> h.getStartDate().isBefore(dateTime) || h.getStartDate() == dateTime || h.getEndDate() == dateTime)
                .collect(Collectors.toList());

        return holidays.size() != 0;
    }

    private void createReplacement(User owner, Holiday request, Long substituteId){
        User substitute = userRepository.findById(substituteId).get();
        Substitute replacement = Substitute.builder()
                .substitute(substitute)
                .holiday(request)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .teamLead(owner).build();

        this.substituteRepository.save(replacement);
    }

    private void createDetailedHoliday(User user, Holiday holiday){
        DetailedHoliday detailedHoliday = DetailedHoliday.builder()
                .holiday(holiday)
                .user(user)
                .creationDate(now())
                .build();

        this.detailedHolidayRepository.save(detailedHoliday);
    }

    private User findSubstituteForTeamlead(User employee){
        User teamLeader = employee.getTeam().getTeamLeader();

        List<Substitute> substitutes = this.substituteRepository.findAll().stream().filter(s -> s.getTeamLead().equals(teamLeader)).filter(s -> { return s.getStartDate().isBefore(now()) || s.getStartDate().equals(now()) || s.getEndDate().equals(now());}).collect(Collectors.toList());

        return substitutes.get(0).getSubstitute();
    }

    @Override
    public HolidayDto createHoliday(HolidayDto holidayDto, Long substituteId) {
        HolidayDto updatedHolidayDto = this.setStatusHoliday(holidayDto);

        Holiday entityToSave = holidayMapper.dtoToEntity(updatedHolidayDto);
        Holiday saved = holidayRepository.save(entityToSave);


        if(saved.getStatus() == HolidayStatus.APPROVED || saved.getStatus() == HolidayStatus.PENDING) {
            this.decreaseNoHolidays(saved);
        }

        User user = userRepository.findById(saved.getUser().getId()).get();
        User teamLeader = user.getTeam().getTeamLeader();

        createDetailedHoliday(user, saved);

        HolidayDto savedHoliday = holidayMapper.entityToDto(saved);

        if(user.getType() == UserType.TEAMLEAD){
            createReplacement(user, saved, substituteId);
            sendNotificationToSubstitute(savedHoliday, NotificationType.MADE_SUBSTITUTE, this.userRepository.getById(substituteId));
        }

        //send notification only if the user is part of a team
        if(user.getTeam()!=null && saved.getStatus()== HolidayStatus.PENDING) {
            if (!isTeamLeadInHoliday(teamLeader))
                sendNotificationToTeamLead(savedHoliday, NotificationType.SENT);
            else {
                sendNotificationToSubstitute(savedHoliday, NotificationType.SENT, findSubstituteForTeamlead(user));
            }
        }

        return holidayMapper.entityToDto(saved);
    }

    private void decreaseNoHolidays(Holiday holiday){
        Integer noHolidays = getNoHolidays(holiday.getStartDate(), holiday.getEndDate());

        if(holiday.getType() == HolidayType.REST) {

            updateUserNoHolidays(userRepository.getById(holiday.getUser().getId()), noHolidays);

        } else if(holiday.getType() == HolidayType.UNPAID){

            Integer noDays = this.getNoUnpaidDays(noHolidays);
            updateUserNoHolidays(userRepository.getById(holiday.getUser().getId()), noDays);

        } else if(holiday.getType() == HolidayType.SPECIAL){

            updateUserNoHolidays(userRepository.getById(holiday.getUser().getId()), 0);
        }
    }

    private void increaseNoHolidays(Holiday holiday){
        Integer noHolidays = getNoHolidays(holiday.getStartDate(), holiday.getEndDate());

        if(holiday.getType() == HolidayType.REST) {

            getBackUserNoHolidays(userRepository.getById(holiday.getUser().getId()), noHolidays);

        } else if(holiday.getType() == HolidayType.UNPAID){

            Integer noDays = this.getNoUnpaidDays(noHolidays);
            getBackUserNoHolidays(userRepository.getById(holiday.getUser().getId()), noDays);

        } else if(holiday.getType() == HolidayType.SPECIAL){

            getBackUserNoHolidays(userRepository.getById(holiday.getUser().getId()), 0);
        }
    }

    private void ChangeHolidayData(HolidayDto dto, Holiday u){
        if(dto.getDetails() != null) {
            u.setDetails(dto.getDetails());
        }
        if(dto.getStatus() != null) {
            u.setStatus(dto.getStatus());
        }
        if(dto.getEndDate() != null) {
            u.setEndDate(dto.getEndDate());
        }
        if(dto.getStartDate() != null) {
            u.setStartDate(dto.getStartDate());
        }
        if(dto.getSubstitute() != null) {
            u.setSubstitute(dto.getSubstitute());
        }
        if(dto.getDocument() != null) {
            u.setDocument(dto.getDocument());
        }
    }

    @Override
    public HolidayDto updateHolidayRequest(HolidayDto holidayDto, Long substituteId) {
        Holiday holiday = holidayRepository.findByID(holidayDto.getId());
        User user = userRepository.findById(holiday.getUser().getId()).get();
        if(user.getType() == UserType.TEAMLEAD){
            holidayDto.setStatus(HolidayStatus.APPROVED);
        }
        else {
            holidayDto.setStatus(HolidayStatus.PENDING);
        }

        return this.updateHoliday(holidayDto, substituteId);
    }

    private void updateReplacement(Substitute substitute, Holiday request, Long substituteId){
        substitute.setHoliday(request);
        substitute.setEndDate(request.getEndDate());
        substitute.setStartDate(request.getStartDate());
        substitute.setSubstitute(this.userRepository.getById(substituteId));

        this.substituteRepository.save(substitute);
    }

    private void updateDetailedHoliday(Holiday oldRequest, Holiday newRequest)
    {
        DetailedHoliday detailedHoliday = this.detailedHolidayRepository.findByHoliday(oldRequest);

        detailedHoliday.setHoliday(newRequest);
        detailedHoliday.setCreationDate(now());

        this.detailedHolidayRepository.save(detailedHoliday);
    }
    @Override
    public HolidayDto updateHoliday(HolidayDto holidayDto, Long substituteId) {
        Holiday u = holidayRepository.findByID(holidayDto.getId());
        u.setDetails(null);

        User user = u.getUser();

        if(u!= null) {

            Holiday holiday = holidayRepository.findByID(holidayDto.getId());

            if(u.getType().equals(UserType.TEAMLEAD)){
                Substitute substitute = this.substituteRepository.findByHoliday(holiday);
                if(substitute.getSubstitute().getId() != substituteId){

                    sendNotificationToSubstitute(holidayMapper.entityToDto(u), NotificationType.MADE_SUBSTITUTE, this.userRepository.getById(substituteId));
                    sendNotificationToSubstitute(holidayMapper.entityToDto(u), NotificationType.END_SUBSTITUTE, substitute.getSubstitute());

                    ChangeHolidayData(holidayDto,u);
                    Holiday updatedHoliday = holidayRepository.save(u);

                    this.updateReplacement(substitute, updatedHoliday, substituteId);
                    this.updateDetailedHoliday(holiday, updatedHoliday);

                    return holidayMapper.entityToDto(updatedHoliday);

                } else {
                    sendNotificationToSubstitute(holidayMapper.entityToDto(u), NotificationType.UPDATE_SUBSTITUTE, substitute.getSubstitute());

                    ChangeHolidayData(holidayDto,u);
                    Holiday updatedHoliday = holidayRepository.save(u);

                    this.updateReplacement(substitute, updatedHoliday, substituteId);
                    this.updateDetailedHoliday(holiday, updatedHoliday);

                    return holidayMapper.entityToDto(updatedHoliday);
                }
            }

            if(u.getUser().getType() == UserType.EMPLOYEE ){
                User teamLeader = user.getTeam().getTeamLeader();
                if(this.isTeamLeadInHoliday(teamLeader)){
                    sendNotificationToSubstitute(holidayMapper.entityToDto(u), NotificationType.UPDATE_SUBSTITUTE, findSubstituteForTeamlead(user));

                    ChangeHolidayData(holidayDto,u);
                    Holiday updatedHoliday = holidayRepository.save(u);

                    this.updateDetailedHoliday(holiday, updatedHoliday);

                    return holidayMapper.entityToDto(updatedHoliday);
                } else {
                    sendNotificationToTeamLead(holidayMapper.entityToDto(u), NotificationType.UPDATE);

                    ChangeHolidayData(holidayDto,u);
                    Holiday updatedHoliday = holidayRepository.save(u);

                    this.updateDetailedHoliday(holiday, updatedHoliday);

                    return holidayMapper.entityToDto(updatedHoliday);
                }
            }
        }

        return holidayDto;
    }

    @Override
    public List<HolidayDto> getAll() {
        List<Holiday> entities = holidayRepository.findAll();
        return holidayMapper.entitiesToDtos(entities);
    }

    @Override
    public List<HolidayDto> getUsersHolidays(Long id) {
        List<Holiday> entities = holidayRepository.findUsersHolidays(id);
        return holidayMapper.entitiesToDtos(entities);
    }
    private void sendNotificationDeleteRequest(User sender, User receiver, NotificationType type){

        UserWithTeamIdDto receiverDto = UserWithTeamIdDto.builder()
                .id(receiver.getId()).email(receiver.getEmail()).forname(receiver.getForname()).surname(receiver.getSurname()).department(receiver.getDepartment())
                .role(receiver.getRole()).nrHolidays(receiver.getNrHolidays()).type(receiver.getType()).teamId(receiver.getTeam().getId())
                .build();

        UserWithTeamIdDto senderDto = UserWithTeamIdDto.builder()
                .id(sender.getId()).email(sender.getEmail()).forname(sender.getForname()).surname(sender.getSurname()).department(sender.getDepartment())
                .role(sender.getRole()).nrHolidays(sender.getNrHolidays()).type(sender.getType()).teamId(sender.getTeam().getId())
                .build();
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setReceiver(receiverDto);
        notificationDto.setSender(senderDto);
        notificationDto.setType(type);
        notificationDto.setSendDate(now());
        notificationDto.setSeen(false);
        notificationDto.setRequest(null);

        notificationService.createNotification(notificationDto);
    }

    @Override
    public HolidayDto deleteHoliday(Long id) {
        Holiday holiday = holidayRepository.findByID(id);
        Substitute s = this.substituteRepository.findByHoliday(holiday);
        HolidayDto holidayDto = holidayMapper.entityToDto(holiday);


        if (holiday != null) {
            if ( holiday.getStatus().equals(HolidayStatus.APPROVED) || holiday.getStatus().equals(HolidayStatus.PENDING)){
                this.increaseNoHolidays(holiday);
            }
            //send notification
            if(holidayDto.getUser().getType()==UserType.EMPLOYEE) {
                if(s!=null){//send notif to the substitute
                    sendNotificationDeleteRequest(userWithTeamIdDtoMapper.dtoToEntity(holidayDto.getUser()),s.getSubstitute(), NotificationType.CANCELED_SUBSTITUTE);
                }
                else { //send notif to the teamlead
                    User sender = userRepository.getById(holidayDto.getUser().getId()); // the user that made the holiday request
                    User receiver = teamRepository.getById(sender.getTeam().getId()).getTeamLeader();
                    sendNotificationDeleteRequest(sender, receiver,NotificationType.CANCELED);
                }
            }
            else if(holiday.getUser().getType()==UserType.TEAMLEAD ){
                if(s!=null){ //there is substitute
                    sendNotificationDeleteRequest(s.getTeamLead(),s.getSubstitute(),NotificationType.END_SUBSTITUTE);
                }
            }

        }

        this.detailedHolidayRepository.delete(this.detailedHolidayRepository.findByHoliday(holiday));
        if(holiday.getUser().getType().equals(UserType.TEAMLEAD)){
            if(s!=null) {// if the user is a teamlead and there is a substitute
                this.substituteRepository.delete(s);
            }
        }
        holidayRepository.delete(holiday);
        return holidayDto;
    }



    @Override
    public List<HolidayDto> getRequestsByType(Long teamLeaderId, HolidayType type) {
        List<Holiday> entities = this.holidayRepository.findAllByTypeAndUserId(type, teamLeaderId);

        return holidayMapper.entitiesToDtos(entities);
    }

    @Override
    public List<HolidayDto> getRequestsByStatus(Long teamLeaderId, HolidayStatus status) {

        List<Holiday> entities = this.holidayRepository.findAllByStatusAndUserId(status, teamLeaderId);

        return holidayMapper.entitiesToDtos(entities);
    }

    @Override
    public List<HolidayDto> getRequestsByStatusAndType(Long teamLeaderId, HolidayStatus status, HolidayType type) {
        List<Holiday> entities = this.holidayRepository.findAllByStatusAndTypeAndUserId(status, type, teamLeaderId);

        return holidayMapper.entitiesToDtos(entities);
    }

    private void updateUserNoHolidays(User userToUpdate, Integer noHolidays){
        userToUpdate.setNrHolidays(userToUpdate.getNrHolidays() - noHolidays);
        userRepository.save(userToUpdate);
    }

    private void getBackUserNoHolidays(User userToUpdate, Integer noHolidays){
        userToUpdate.setNrHolidays(userToUpdate.getNrHolidays() + noHolidays);
        userRepository.save(userToUpdate);
    }

    private void sendNotificationToTeamLead(HolidayDto holidayDto,NotificationType type){
        User sender = userRepository.getById(holidayDto.getUser().getId()); // the user that made the holiday request
        User receiver = teamRepository.getById(sender.getTeam().getId()).getTeamLeader();
        UserWithTeamIdDto receiverDto = UserWithTeamIdDto.builder()
                .id(receiver.getId()).email(receiver.getEmail()).forname(receiver.getForname()).surname(receiver.getSurname()).department(receiver.getDepartment())
                .role(receiver.getRole()).nrHolidays(receiver.getNrHolidays()).type(receiver.getType()).teamId(receiver.getTeam().getId())
                .build();

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setReceiver(receiverDto);
        notificationDto.setSender(holidayDto.getUser());
        notificationDto.setType(type);
        notificationDto.setSendDate(now());
        notificationDto.setSeen(false);
        notificationDto.setRequest(holidayDto);

        notificationService.createNotification(notificationDto);
    }

    private void sendNotificationToSubstitute(HolidayDto holidayDto, NotificationType type, User receiver){
        User sender = userRepository.getById(holidayDto.getUser().getId()); // the user that made the holiday request

        UserWithTeamIdDto receiverDto = UserWithTeamIdDto.builder()
                .id(receiver.getId()).email(receiver.getEmail()).forname(receiver.getForname()).surname(receiver.getSurname()).department(receiver.getDepartment())
                .role(receiver.getRole()).nrHolidays(receiver.getNrHolidays()).type(receiver.getType()).teamId(receiver.getTeam().getId())
                .build();

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setReceiver(receiverDto);
        notificationDto.setSender(holidayDto.getUser());
        notificationDto.setType(type);
        notificationDto.setSendDate(now());
        notificationDto.setSeen(false);
        notificationDto.setRequest(holidayDto);

        notificationService.createNotification(notificationDto);
    }

    private void sendNotificationToEmployee(HolidayDto holidayDto, NotificationType type){
        User receiver = userRepository.getById(holidayDto.getUser().getId()); // the user that made the holiday request
        User sender = teamRepository.getById(receiver.getTeam().getId()).getTeamLeader();
        UserWithTeamIdDto senderDto = UserWithTeamIdDto.builder()
                .id(sender.getId()).email(sender.getEmail()).forname(sender.getForname()).surname(sender.getSurname()).department(sender.getDepartment())
                .role(sender.getRole()).nrHolidays(sender.getNrHolidays()).type(sender.getType()).teamId(sender.getTeam().getId())
                .build();

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setReceiver(holidayDto.getUser());
        notificationDto.setSender(senderDto);
        notificationDto.setType(type);
        notificationDto.setSendDate(now());
        notificationDto.setSeen(false);
        notificationDto.setRequest(holidayDto);

        notificationService.createNotification(notificationDto);
    }
    @Override
    public HolidayDto approveHolidayRequest(Long id) {
        HolidayDto holidayDto = holidayMapper.entityToDto(holidayRepository.getById(id));
        HolidayDto updated;

        holidayDto.setStatus(HolidayStatus.APPROVED);
        holidayDto.setDetails(null);
        sendNotificationToEmployee(holidayDto,NotificationType.APPROVED);

        return holidayMapper.entityToDto(holidayRepository.save(holidayMapper.dtoToEntity(holidayDto)));
    }

    @Override
    public HolidayDto denyHolidayRequest(Long id) {
        HolidayDto holidayDto = holidayMapper.entityToDto(holidayRepository.getById(id));
        holidayDto.setStatus(HolidayStatus.DENIED);
        holidayDto.setDetails(null);

        Holiday holiday = holidayMapper.dtoToEntity(holidayDto);

        increaseNoHolidays(holiday);

        sendNotificationToEmployee(holidayDto,NotificationType.DENIED);

        return holidayMapper.entityToDto(holidayRepository.save(holidayMapper.dtoToEntity(holidayDto)));
    }

    @Override
    public HolidayDto requestMoreDetails(UpdateDetailsHolidayDto updateDetailsHolidayDto) {
        HolidayDto holidayDto = holidayMapper.entityToDto(holidayRepository.getById(updateDetailsHolidayDto.getId()));
        holidayDto.setDetails(updateDetailsHolidayDto.getDetails());

        sendNotificationToEmployee(holidayDto,NotificationType.MORE_DETAILS);

        return holidayMapper.entityToDto(holidayRepository.save(holidayMapper.dtoToEntity(holidayDto)));

    }

    private boolean isWeekend(final LocalDateTime ld)
    {
        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY;
    }

    public Integer getNoUnpaidDays(Integer days){
        return days / 10;
    }

    public Integer getNoHolidays(LocalDateTime start, LocalDateTime end){
        Integer noHolidays = 0;

        for (LocalDateTime date = start; date.isBefore(end.plusDays(1)); date = date.plusDays(1))
        {
            if(!isWeekend(date)) {
                noHolidays++;
            }
        }
        return noHolidays;
    }

    @Override
    public HolidayDto getHolidayById(Long id) {
        return this.holidayMapper.entityToDto(this.holidayRepository.findByID(id));
    }

    @Override
    public List<HolidayDto> filterByTypeAndUserName(HolidayTypeAndUserName dto) {
        String forname = dto.getForname();
        String surname = dto.getSurname();
        Long teamLeaderId = dto.getTeamLeaderId();
        HolidayType type = dto.getType();


        User teamlead = this.userRepository.getById(teamLeaderId);
        List<User> members = this.teamRepository.getById(teamlead.getTeam().getId()).getMembers();

        List<Holiday> holidays = new ArrayList<>();

        if(forname != null && surname == null){
            if(type == null) {
                members = new ArrayList<>();
            }
            else {
                members = new ArrayList<>();
            }
        } else if(forname == null && surname != null){
            if(type == null){
                members.forEach(member -> {
                            if (!member.getType().name().equals("TEAMLEAD") && (member.getSurname().toLowerCase().contains(surname.toLowerCase()) || member.getForname().toLowerCase().contains(surname.toLowerCase())))
                                holidays.addAll(this.holidayRepository.findByUserId(member.getId()));
                        }
                );
            }
            else {
                members.forEach(member -> {
                            if (!member.getType().name().equals("TEAMLEAD") && (member.getSurname().toLowerCase().contains(surname.toLowerCase()) || member.getForname().toLowerCase().contains(surname.toLowerCase())))
                                holidays.addAll(this.holidayRepository.findByUserId(member.getId()).stream().filter(h -> h.getType() == type).collect(Collectors.toList()));
                        }
                );
            }
        } else if(forname != null && surname != null){
            if(type == null){
                members.forEach(member -> {
                            if (!member.getType().name().equals("TEAMLEAD") && member.getForname().toLowerCase().contains(forname.toLowerCase()) && member.getSurname().toLowerCase().contains(surname.toLowerCase()))
                                holidays.addAll(this.holidayRepository.findByUserId(member.getId()));
                        }
                );
            } else {
                members.forEach(member -> {
                            if (!member.getType().name().equals("TEAMLEAD") && member.getForname().toLowerCase().contains(forname.toLowerCase()) && member.getSurname().toLowerCase().contains(surname.toLowerCase()))
                                holidays.addAll(this.holidayRepository.findByUserId(member.getId()).stream().filter(h -> h.getType() == type).collect(Collectors.toList()));
                        }
                );
            }
        } else {
            if(type == null){
                members.forEach(member -> {
                            if (!member.getType().name().equals("TEAMLEAD"))
                                holidays.addAll(this.holidayRepository.findByUserId(member.getId()));
                        }
                );
            } else {
                members.forEach(member -> {
                            if (!member.getType().name().equals("TEAMLEAD"))
                                holidays.addAll(this.holidayRepository.findByUserId(member.getId()).stream().filter(h -> h.getType() == type).collect(Collectors.toList()));
                        }
                );
            }
        }

        return holidayMapper.entitiesToDtos(holidays);

    }

    @Override
    public List<HolidayDto> filterByType(Long teamLeaderId, HolidayType type) {
        User teamlead = this.userRepository.getById(teamLeaderId);

        List<User> members = this.teamRepository.getById(teamlead.getTeam().getId()).getMembers();

        List<Holiday> holidays = new ArrayList<>();


        members.forEach(member -> {
                    if (!member.getType().name().equals("TEAMLEAD"))
                        holidays.addAll(this.holidayRepository.findByUserId(member.getId()).stream().filter(h -> h.getType().name().equals(type)).collect(Collectors.toList()));
                }
        );

        return holidayMapper.entitiesToDtos(holidays);
    }

    @Override
    public List<HolidayDto> filterByUserName(Long teamLeaderId, String forname, String surname) {

        User teamlead = this.userRepository.getById(teamLeaderId);
        List<User> members = this.teamRepository.getById(teamlead.getTeam().getId()).getMembers();

        List<Holiday> holidays = new ArrayList<>();

        if(forname != null && surname == null){
            members.forEach(member -> {
                        if (!member.getType().name().equals("TEAMLEAD") && member.getForname().contains(forname))
                            holidays.addAll(this.holidayRepository.findByUserId(member.getId()));
                    }
            );
        } else if(forname == null && surname != null){
            members.forEach(member -> {
                        if (!member.getType().name().equals("TEAMLEAD") && member.getSurname().contains(surname))
                            holidays.addAll(this.holidayRepository.findByUserId(member.getId()));
                    }
            );
        } else if(forname != null && surname != null){
            members.forEach(member -> {
                        if (!member.getType().name().equals("TEAMLEAD") && member.getForname().contains(forname) && member.getSurname().contains(surname))
                            holidays.addAll(this.holidayRepository.findByUserId(member.getId()));
                    }
            );
        }

        return holidayMapper.entitiesToDtos(holidays);
    }
    @Override
    public Integer checkRequestCreate(String email, HolidayType type, String startDate, String endDate) {
        User user = this.userRepository.findByEmail(email);
        LocalDateTime sD = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime eD = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Integer numberOfRequiredDays = this.getNoHolidays(sD, eD);
            Integer unpaidRequiredDays = numberOfRequiredDays / 10;
            Integer userNoHolidays = user.getNrHolidays();
            log.info("Service - checkResult   " + type + " " + type.toString());

            if (numberOfRequiredDays > userNoHolidays && type == HolidayType.REST) {
                return 0;
            } else if (unpaidRequiredDays > userNoHolidays && type == HolidayType.UNPAID) {
                return 0;
            }

        return 1;
    }


    @Override
    public Integer checkRequestUpdate(String email, HolidayType type, String startDate, String endDate, Long holidayId) {
        User user = this.userRepository.findByEmail(email);
        Holiday holiday = this.holidayRepository.findByID(holidayId);

        if(holiday.getStatus() == HolidayStatus.DENIED) {
            decreaseNoHolidays(holiday);
        }

        LocalDateTime sD =  LocalDateTime. parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime eD = LocalDateTime. parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Integer numberOfRequiredDaysInitialRequest = this.getNoHolidays(holiday.getStartDate(), holiday.getEndDate());
        Integer unpaidRequiredDaysInitialRequest = this.getNoUnpaidDays(numberOfRequiredDaysInitialRequest);

        Integer numberOfRequiredDays = this.getNoHolidays(sD, eD);
        Integer unpaidRequiredDays = this.getNoUnpaidDays(numberOfRequiredDays);

        Integer userNoHolidays = user.getNrHolidays();

        Integer daysToBeTakenOrAdded = 0;

        if(type == HolidayType.REST){
            daysToBeTakenOrAdded = numberOfRequiredDays - numberOfRequiredDaysInitialRequest;

            if(daysToBeTakenOrAdded > userNoHolidays){
                return 0;
            }
            else {
                this.updateUserNoHolidays(user, daysToBeTakenOrAdded);
                return 1;
            }
        } else if(type == HolidayType.UNPAID){
            daysToBeTakenOrAdded = unpaidRequiredDays - unpaidRequiredDaysInitialRequest;

            if(daysToBeTakenOrAdded > userNoHolidays){
                return 0;
            }
            else {
                this.updateUserNoHolidays(user, daysToBeTakenOrAdded);
                return 1;
            }
        }
        else {
            return 1;
        }
    }

    @Override
    public Integer checkIfDatesOverlap(String email, String startDate, String endDate) {
        User user = this.userRepository.findByEmail(email);
        LocalDateTime sD = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime eD = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<Holiday> hds = holidayRepository.findUsersHolidays(user.getId());
        for(Holiday x : hds){
            if(!(x.getStatus().equals(HolidayStatus.DENIED)) && (sD.isBefore(x.getStartDate()) && eD.isAfter(x.getStartDate()) ||
                    sD.isBefore(x.getEndDate()) && eD.isAfter(x.getEndDate()) ||
                    sD.isBefore(x.getStartDate()) && eD.isAfter(x.getEndDate()) ||
                    sD.isAfter(x.getStartDate()) && eD.isBefore(x.getEndDate()) ||
                    sD.isEqual(x.getStartDate()) || sD.isEqual(x.getEndDate()) ||
                    eD.isEqual(x.getStartDate()) || eD.isEqual(x.getEndDate())))
                return 0;
        };

        return 1;
    }
    @Override
    public Integer checkIfDatesOverlapUpdate(String email, String startDate, String endDate, Long holidayId) {
        User user = this.userRepository.findByEmail(email);
        LocalDateTime sD = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime eD = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<Holiday> hds = holidayRepository.findByUserId(user.getId()).stream().filter(x -> !x.getId().equals(holidayId)).collect(Collectors.toList());

        for(Holiday x : hds){
            if(!(x.getStatus().equals(HolidayStatus.DENIED)) && (sD.isBefore(x.getStartDate()) && eD.isAfter(x.getStartDate()) ||
                    sD.isBefore(x.getEndDate()) && eD.isAfter(x.getEndDate()) ||
                    sD.isBefore(x.getStartDate()) && eD.isAfter(x.getEndDate()) ||
                    sD.isAfter(x.getStartDate()) && eD.isBefore(x.getEndDate()) ||
                    sD.isEqual(x.getStartDate()) || sD.isEqual(x.getEndDate()) ||
                    eD.isEqual(x.getStartDate()) || eD.isEqual(x.getEndDate()))) {
                return 0;
            }
        };

        return 1;
    }

    private User getTeamLeaderForUser(User user){
        Team team = teamRepository.getById(user.getTeam().getId());
        return userRepository.getById(team.getTeamLeader().getId());
    }


    private boolean checkRequestCreatedWhileTeamleadGone(DetailedHoliday d, Substitute s){
        if(d.getCreationDate().isAfter(s.getStartDate()) || d.getCreationDate().equals(s.getStartDate()) || d.getCreationDate().equals(s.getEndDate()))
            return true;
        return false;
    }

    @Override
    public List<HolidayDto> getRequestsForSubstitute(Long substituteId) {
        LocalDateTime now = now();

        Set<Holiday> holidays = new HashSet<>();

        //We take all the substitutes which have the substitute the one with the id substituteId
        List<Substitute> substitutes = substituteRepository.findAll()
                .stream()
                .filter( s -> (s.getStartDate().isBefore(now) || s.getStartDate().isEqual(now)) && (now.isBefore(s.getEndDate()) || now.isEqual(s.getEndDate())))
                .filter( s -> s.getSubstitute().getId().equals(substituteId))
               .collect(Collectors.toList());

        List<DetailedHoliday> detailedSubstitutes = new ArrayList<>();


        // We take all the detailed holidays requests of the users for which the teamleaders have the given substitute active (the one with substituteId), parse them and check for each of them if they were created
        // after the teamlead went in vacation
        holidayRepository.findAll().stream()
                .filter(h -> {return substituteService.teamLeadersForWhichSubstituteIsActive(substituteId).contains(this.userMapper.entityToDto(h.getUser().getTeam().getTeamLeader())) && !h.getUser().getType().equals(UserType.TEAMLEAD);})
                .map(h -> this.detailedHolidayRepository.findByHoliday(h))
                .forEach(h ->
                {
                    substitutes.forEach(s ->
                    {
                        if(this.checkRequestCreatedWhileTeamleadGone(h, s)){
                            holidays.add(this.holidayRepository.findByID(h.getHoliday().getId()));
                        }
                    });
                });

        log.info("Am ajuns in substitute " + holidays.size());
        List<Holiday> holidayList = new ArrayList<>();
        holidayList.addAll(holidays);
        return this.holidayMapper.entitiesToDtos(holidayList);

    }

}
