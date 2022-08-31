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
import com.internship.holiday_manager.service.emailing_service.EmailingService;
import com.internship.holiday_manager.service.notification_service.NotificationService;
import com.internship.holiday_manager.service.substitute.SubstituteService;
import com.internship.holiday_manager.service.teamlead_service.TeamLeadService;
import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
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
    private final EmailingService emailingService;
    private final HolidayMapper holidayMapper;
    private final UserMapper userMapper;
    private final UserWithTeamIdMapper userWithTeamIdDtoMapper;

    private final DetailedHolidayRepository detailedHolidayRepository;
    public HolidayServiceImpl(TeamLeadService teamLeadService, UserRepository userRepository, HolidayRepository holidayRepository, TeamRepository teamRepository, NotificationService notificationService, EmailingService emailingService, HolidayMapper holidayMapper, SubstituteRepository substituteRepository, SubstituteService substituteService, UserMapper userMapper, UserWithTeamIdMapper userWithTeamIdDto, DetailedHolidayRepository detailedHolidayRepository) {

        this.userRepository = userRepository;
        this.holidayRepository = holidayRepository;
        this.teamRepository = teamRepository;
        this.notificationService = notificationService;
        this.teamLeadService = teamLeadService;
        this.emailingService = emailingService;
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

        List<Substitute> substitutes = this.substituteRepository.findAll().stream().filter(s -> s.getTeamLead().equals(teamLeader)).filter(s -> { return (s.getStartDate().isBefore(now()) || s.getStartDate().isEqual(now())) && (now().isBefore(s.getEndDate()) || now().isEqual(s.getEndDate()));}).collect(Collectors.toList());

        if(substitutes.size() == 1){
            return substitutes.get(0).getSubstitute();
        }
        return null;
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
        Holiday holiday = holidayRepository.findByID(holidayDto.getId());
        holiday.setDetails(null);

        User user = this.userRepository.getById(holiday.getUser().getId());

        if(holiday!= null) {

            if(user.getType().equals(UserType.TEAMLEAD)){
                Substitute substitute = this.substituteRepository.findByHoliday(holiday);
                if(substitute.getSubstitute().getId() != substituteId){

                    sendNotificationToSubstitute(holidayMapper.entityToDto(holiday), NotificationType.MADE_SUBSTITUTE, this.userRepository.getById(substituteId));
                    sendNotificationToSubstitute(holidayMapper.entityToDto(holiday), NotificationType.END_SUBSTITUTE, substitute.getSubstitute());

                    ChangeHolidayData(holidayDto,holiday);
                    Holiday updatedHoliday = holidayRepository.save(holiday);

                    this.updateReplacement(substitute, updatedHoliday, substituteId);
                    this.updateDetailedHoliday(holiday, updatedHoliday);

                    return holidayMapper.entityToDto(updatedHoliday);

                } else {
                    sendNotificationToSubstitute(holidayMapper.entityToDto(holiday), NotificationType.UPDATE_SUBSTITUTE, substitute.getSubstitute());

                    ChangeHolidayData(holidayDto,holiday);
                    Holiday updatedHoliday = holidayRepository.save(holiday);

                    this.updateReplacement(substitute, updatedHoliday, substituteId);
                    this.updateDetailedHoliday(holiday, updatedHoliday);

                    return holidayMapper.entityToDto(updatedHoliday);
                }
            }

            if(user.getType() == UserType.EMPLOYEE ){
                User teamLeader = user.getTeam().getTeamLeader();
                if(this.isTeamLeadInHoliday(teamLeader)){
                    sendNotificationToSubstitute(holidayMapper.entityToDto(holiday), NotificationType.UPDATE_SUBSTITUTE, findSubstituteForTeamlead(user));

                    ChangeHolidayData(holidayDto,holiday);
                    Holiday updatedHoliday = holidayRepository.save(holiday);

                    this.updateDetailedHoliday(holiday, updatedHoliday);

                    return holidayMapper.entityToDto(updatedHoliday);
                } else {
                    sendNotificationToTeamLead(holidayMapper.entityToDto(holiday), NotificationType.UPDATE);

                    ChangeHolidayData(holidayDto,holiday);
                    Holiday updatedHoliday = holidayRepository.save(holiday);

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
        User user = holiday.getUser();
        User teamLeader = user.getTeam().getTeamLeader();
        User substituter = this.findSubstituteForTeamlead(teamLeader);

        HolidayDto holidayDto = holidayMapper.entityToDto(holiday);


        if (holiday != null) {
            if ( holiday.getStatus().equals(HolidayStatus.APPROVED) || holiday.getStatus().equals(HolidayStatus.PENDING)){
                this.increaseNoHolidays(holiday);
            }
            //send notification
            if(holidayDto.getUser().getType()==UserType.EMPLOYEE) {
                if(substituter!=null){//send notif to the substitute
                    sendNotificationDeleteRequest(user,substituter, NotificationType.CANCELED_SUBSTITUTE);
                }
                else { //send notif to the teamlead
                    User sender = userRepository.getById(holidayDto.getUser().getId()); // the user that made the holiday request
                    User receiver = teamRepository.getById(sender.getTeam().getId()).getTeamLeader();
                    sendNotificationDeleteRequest(sender, receiver,NotificationType.CANCELED);
                }
            }
            else if(holiday.getUser().getType()==UserType.TEAMLEAD ){
                Substitute s = this.substituteRepository.findByHoliday(holiday);

                if(s!=null){ //there is substitute
                    sendNotificationDeleteRequest(s.getTeamLead(),s.getSubstitute(),NotificationType.END_SUBSTITUTE);
                }
            }

        }

        this.detailedHolidayRepository.delete(this.detailedHolidayRepository.findByHoliday(holiday));
        if(holiday.getUser().getType().equals(UserType.TEAMLEAD)){
            Substitute s = this.substituteRepository.findByHoliday(holiday);

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

    private void sendNotificationToEmployeeFromSubstitute(HolidayDto holidayDto, NotificationType type, User substitute){
        User receiver = userRepository.getById(holidayDto.getUser().getId()); // the user that made the holiday request
        UserWithTeamIdDto senderDto = UserWithTeamIdDto.builder()
                .id(substitute.getId()).email(substitute.getEmail()).forname(substitute.getForname()).surname(substitute.getSurname()).department(substitute.getDepartment())
                .role(substitute.getRole()).nrHolidays(substitute.getNrHolidays()).type(substitute.getType()).teamId(substitute.getTeam().getId())
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

        Holiday holiday = this.holidayRepository.getById(id);
        User user = holiday.getUser();
        User teamLeader = user.getTeam().getTeamLeader();
        User substitute = this.findSubstituteForTeamlead(teamLeader);

        holidayDto.setStatus(HolidayStatus.APPROVED);
        holidayDto.setDetails(null);

        if(substitute != null){
            sendNotificationToEmployeeFromSubstitute(holidayDto, NotificationType.APPROVED_SUBSTITUTE, substitute);
        }
        else {
            sendNotificationToEmployee(holidayDto,NotificationType.APPROVED);
        }

        return holidayMapper.entityToDto(holidayRepository.save(holidayMapper.dtoToEntity(holidayDto)));
    }

    @Override
    public HolidayDto denyHolidayRequest(Long id) {
        Holiday holiday = this.holidayRepository.getById(id);
        HolidayDto holidayDto = holidayMapper.entityToDto(holidayRepository.getById(id));
        User user = holiday.getUser();
        User teamLeader = user.getTeam().getTeamLeader();
        User substitute = this.findSubstituteForTeamlead(teamLeader);

        if(substitute != null){
            sendNotificationToEmployeeFromSubstitute(holidayDto, NotificationType.DENIED_SUBSTITUTE, substitute);
        }
        else {
            sendNotificationToEmployee(holidayDto,NotificationType.DENIED);
        }

        holidayDto.setStatus(HolidayStatus.DENIED);
        holidayDto.setDetails(null);

        increaseNoHolidays(holiday);

        return holidayMapper.entityToDto(holidayRepository.save(holidayMapper.dtoToEntity(holidayDto)));
    }

    @Override
    public HolidayDto requestMoreDetails(UpdateDetailsHolidayDto updateDetailsHolidayDto) {
        Holiday holiday = this.holidayRepository.getById(updateDetailsHolidayDto.getId());
        User user = holiday.getUser();
        User teamLeader = user.getTeam().getTeamLeader();
        User substitute = this.findSubstituteForTeamlead(teamLeader);

        HolidayDto holidayDto = holidayMapper.entityToDto(holidayRepository.getById(updateDetailsHolidayDto.getId()));
        holidayDto.setDetails(updateDetailsHolidayDto.getDetails());

        if(substitute != null){
            sendNotificationToEmployeeFromSubstitute(holidayDto, NotificationType.MORE_DETAILS_SUBSTITUTE, substitute);
        }
        else {
            sendNotificationToEmployee(holidayDto,NotificationType.MORE_DETAILS);
        }

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


    public byte[] generateHrPDF(HolidayDto holidayDto) throws MessagingException, DocumentException, IOException {

        log.info(holidayDto.toString());
        Holiday holiday = holidayRepository.getById(holidayDto.getId());
        System.out.println(holiday);
        User emp=holiday.getUser();
        String name = emp.getSurname() + " " +emp.getForname();
        //User teamLead=this.getTeamLeaderForUser(emp);


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4, 35, 35, 25 , 5);

        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();
        document.newPage();
        BaseFont base = BaseFont.createFont(Paths.get("").toAbsolutePath().toString()+"/src/main/java/com/internship/holiday_manager/service/holiday_service/OpenSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont baseBold = BaseFont.createFont(Paths.get("").toAbsolutePath().toString()+"/src/main/java/com/internship/holiday_manager/service/holiday_service/OpenSans-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fontBold = new Font(baseBold, 12f, Font.BOLD);
        Font fontNormal = new Font(base, 12f);
        //document.add(new Paragraph("Hello world +"+ holidayDto.getId().toString()));
        if(holiday.getType()==HolidayType.SPECIAL){



            try{
                Image img = Image.getInstance(Paths.get("").toAbsolutePath().toString()+"/src/main/java/com/internship/holiday_manager/service/holiday_service/MHP_Logo.png");
                img.setAlignment(Element.ALIGN_RIGHT);
                img.scaleToFit(150,63);
                img.setSpacingAfter(40f);
                document.add(img);
                Paragraph documentParagraph = new Paragraph();
                documentParagraph.setSpacingBefore(40f);
                Font titleParagraphFont=new Font(Font.FontFamily.HELVETICA, 12);

                Paragraph titleParagraph = new Paragraph("Cerere de acordare a concediului",titleParagraphFont);
                Paragraph titleParagraph2=new Paragraph("-evenimente speciale -",titleParagraphFont);
                titleParagraph2.setAlignment(Element.ALIGN_CENTER);
                titleParagraph.add(titleParagraph2);
                titleParagraph.setSpacingBefore(45f);
                titleParagraph.setAlignment(Element.ALIGN_CENTER);
                titleParagraph.setSpacingAfter(30f);
                titleParagraph.setFont(fontNormal);
                document.add(titleParagraph);


                Paragraph firstParagraph=new Paragraph();
                documentParagraph.add(firstParagraph);
                firstParagraph.setFont(fontNormal);
                Phrase p1=new Phrase("  Subsemnatul(a) "+emp.getSurname()+" "+emp.getForname()+", angajat(ă) la societatea MHP CONSULTING ROMANIA, înfuncţia de "+emp.getRole()+" department "+emp.getDepartment()+ " vă rog sa-mi aprobaţi efectuarea a "+this.getNoHolidays(holiday.getStartDate(),holiday.getEndDate())+" zi/zile libere plătite în perioada: "+holiday.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" - "+ holiday.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" reprezentând concediu pentru evenimente speciale.");

                Paragraph certificat=new Paragraph("În sprijinul cererii mele, atasez o copie a certificatului. ");
                firstParagraph.add(p1);
                certificat.setSpacingBefore(15f);
                firstParagraph.add(certificat);

                firstParagraph.setFont(fontNormal);
                firstParagraph.setSpacingAfter(15f);
                Paragraph secondParagraph=new Paragraph("Declar  pe  proprie  răspundere  ca  managerul  de  proiect  a  fost  informat despre intenția de a pleca în concediu.");
                secondParagraph.setFont(fontBold);
                secondParagraph.setSpacingAfter(15f);
                documentParagraph.add(secondParagraph);


                Paragraph thirdParagraph=new Paragraph("Asa cum a fost agreat împreuna cu Supervizorul meu, pe durata concediului voi fi înlocuit pe proiecte de către "+holiday.getSubstitute()+".");
                thirdParagraph.setFont(fontBold);
                thirdParagraph.setSpacingAfter(20f);

                documentParagraph.add(thirdParagraph);
                Paragraph userSignatureParagraph=new Paragraph();

                Paragraph multumesc=new Paragraph("Va multumesc!");
                Paragraph userName=new Paragraph(emp.getSurname()+" "+emp.getForname());
                userSignatureParagraph.add(multumesc);
                userSignatureParagraph.add(userName);
                userSignatureParagraph.setSpacingAfter(20f);
                documentParagraph.add(userSignatureParagraph);


                Paragraph aprobareParagraph=new Paragraph();
                aprobareParagraph.setFont(fontNormal);

                Paragraph aprobare=new Paragraph("Se aprobă / Genehmigt, ");
                aprobare.setFont(fontBold);
                Paragraph aprobareName=new Paragraph("Nume și prenume ",fontNormal);
                Paragraph aprobareSignature=new Paragraph("Semnătura ",fontNormal);
                Paragraph aprobareSig=new Paragraph("_________________________________________",fontNormal);
                aprobareParagraph.add(aprobare);
                aprobareParagraph.add(aprobareName);
                aprobareParagraph.add(aprobareSignature);
                aprobareParagraph.add(aprobareSig);
                aprobareParagraph.setAlignment(Element.ALIGN_RIGHT);
                documentParagraph.add(aprobareParagraph);
                document.add(documentParagraph);


            }
            catch (Exception e){e.printStackTrace();}
        }


        if(holiday.getType()==HolidayType.REST){

            try{


                Image img = Image.getInstance(Paths.get("").toAbsolutePath().toString()+"/src/main/java/com/internship/holiday_manager/service/holiday_service/MHP_Logo.png");
                img.setAlignment(Element.ALIGN_RIGHT);
                img.scaleToFit(150,63);
                img.setSpacingAfter(40f);
                document.add(img);
                Paragraph documentParagraph = new Paragraph();
                documentParagraph.setSpacingBefore(40f);


                Paragraph titleParagraph = new Paragraph("Cerere concediu de odihnă / Urlaubsantrag",fontNormal);
                titleParagraph.setSpacingBefore(45f);
                titleParagraph.setAlignment(Element.ALIGN_CENTER);
                titleParagraph.setSpacingAfter(30f);
                document.add(titleParagraph);

                Font firstParagraphFont=new Font(Font.FontFamily.HELVETICA, 12);
                Paragraph firstParagraph=new Paragraph();
                documentParagraph.add(firstParagraph);
                Phrase p1=new Phrase("  Dl/Dna "+emp.getSurname()+" "+emp.getForname()+" angajat in funcția de "+emp.getRole()+" solicit plecarea in concediu pe anul ",fontNormal);
                Font yearPhraseFont=fontNormal;
                Phrase year=new Phrase(holiday.getStartDate().format(DateTimeFormatter.ofPattern("yyyy")));
                year.setFont(yearPhraseFont);
                Phrase p2=new Phrase(" de la data de " +holiday.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " pană la data de "+holiday.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" adică "+this.getNoHolidays(holiday.getStartDate(),holiday.getEndDate())+" zi.",fontNormal);
                firstParagraph.add(p1);
                firstParagraph.add(year);
                firstParagraph.add(p2);
                firstParagraph.setFont(firstParagraphFont);
                firstParagraph.setSpacingAfter(15f);
                Font secondParagraphFont=fontBold;
                Paragraph secondParagraph=new Paragraph("Declar pe proprie răspundere că managerul a fost informat despre intentia de a pleca în concediu.",fontNormal);
                secondParagraph.setFont(secondParagraphFont);
                secondParagraph.setSpacingAfter(15f);
                documentParagraph.add(secondParagraph);


                Font thirdParagraphFont=fontBold;
                Paragraph thirdParagraph=new Paragraph("Asa cum a fost agreat împreuna cu Supervizorul meu "+", pe durata concediului voi fi înlocuit pe proiecte de catre "+holiday.getSubstitute()+".",fontNormal);
                thirdParagraph.setFont(thirdParagraphFont);
                thirdParagraph.setSpacingAfter(20f);
                documentParagraph.add(thirdParagraph);

                Font userSignatureFont=new Font(Font.FontFamily.HELVETICA, 12);
                Paragraph userSignatureParagraph=new Paragraph();
                Paragraph userName=new Paragraph(emp.getSurname()+" "+emp.getForname());

                userSignatureParagraph.add(userName);


                userSignatureParagraph.setAlignment(Element.ALIGN_LEFT);

                userSignatureParagraph.setSpacingAfter(20f);
                documentParagraph.add(userSignatureParagraph);



                Font aprobareParagraphFont=fontNormal;
                Paragraph aprobareParagraph=new Paragraph();
                aprobareParagraph.setFont(aprobareParagraphFont);
                Font aprobareFont=fontBold;
                Paragraph aprobare=new Paragraph("Se aprobă / Genehmigt, ",fontNormal);
                aprobare.setFont(aprobareFont);
                Paragraph aprobareName=new Paragraph("Nume și prenume ",fontNormal);
                Paragraph aprobareSignature=new Paragraph("Semnătura ",fontNormal);
                Paragraph aprobareSig=new Paragraph("_________________________________________");
                aprobareParagraph.add(aprobare);
                aprobareParagraph.add(aprobareName);
                aprobareParagraph.add(aprobareSignature);
                aprobareParagraph.add(aprobareSig);
                aprobareParagraph.setAlignment(Element.ALIGN_RIGHT);
                aprobareParagraph.setSpacingBefore(30);
                documentParagraph.add(aprobareParagraph);
                document.add(documentParagraph);


            }
            catch (Exception e){e.printStackTrace();}

        }

        if(holiday.getType()==HolidayType.UNPAID){

            try{


                Image img = Image.getInstance(Paths.get("").toAbsolutePath().toString()+"/src/main/java/com/internship/holiday_manager/service/holiday_service/MHP_Logo.png");
                img.setAlignment(Element.ALIGN_RIGHT);
                img.scaleToFit(150,63);
                img.setSpacingAfter(40f);
                document.add(img);
                Paragraph topParagraph = new Paragraph();

                Paragraph MHP=new Paragraph("MHP Consulting Romania SRL");

                Paragraph adress=new Paragraph("Strada Onisifor Ghibu, Nr. 20A");
                Chunk glue = new Chunk(new VerticalPositionMark());
                Paragraph judAndApr=new Paragraph("Jud.Cluj");
                judAndApr.add(glue);
                judAndApr.add("Se aprobă");
                Paragraph NrRegAndName=new Paragraph("Nr. inreg...../........");
                NrRegAndName.add(glue);
                NrRegAndName.add("Nume/Prenume");
                NrRegAndName.setSpacingAfter(15f);
                Paragraph aprobareSignature=new Paragraph("Semnatura_________________ ");
                aprobareSignature.setAlignment(Element.ALIGN_RIGHT);
                topParagraph.add(MHP);
                topParagraph.add(adress);
                topParagraph.add(judAndApr);
                topParagraph.add(NrRegAndName);
                topParagraph.add(aprobareSignature);
                document.add(topParagraph);




                Paragraph documentParagraph = new Paragraph();
                documentParagraph.setSpacingBefore(40f);
                Font titleParagraphFont=fontNormal;

                Paragraph titleParagraph = new Paragraph("Către conducerea MHP CONSULTING ROMANIA SRL",titleParagraphFont);
                titleParagraph.setSpacingBefore(45f);
                titleParagraph.setAlignment(Element.ALIGN_CENTER);
                titleParagraph.setSpacingAfter(30f);
                document.add(titleParagraph);

                Font firstParagraphFont=fontNormal;
                Paragraph firstParagraph=new Paragraph();
                documentParagraph.add(firstParagraph);
                Phrase p1=new Phrase("  Subsemnatul "+emp.getSurname()+" "+emp.getForname()+", angajată MHP Consulting Romania SRL, in funcția de "+emp.getRole()+" vă rog să imi aprobati cererea de concediu fară plată pentru studii/ scop personal, in perioada "+holiday.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+"-"+holiday.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),fontNormal);


                firstParagraph.add(p1);

                firstParagraph.setFont(firstParagraphFont);
                firstParagraph.setSpacingAfter(15f);
                Font secondParagraphFont=fontBold;
                Paragraph secondParagraph=new Paragraph("Declar pe proprie răspundere că managerul de proiect a fost informat despre intenția de a pleca în concediu.",fontNormal);
                secondParagraph.setFont(secondParagraphFont);
                secondParagraph.setSpacingAfter(15f);
                documentParagraph.add(secondParagraph);

                Font userSignatureFont=fontNormal;
                Paragraph userSignatureParagraph=new Paragraph();
                Paragraph DateAndAngajat=new Paragraph("Data:"+java.time.LocalDate.now().toString());
                DateAndAngajat.add(glue);
                DateAndAngajat.add("Angajat");
                DateAndAngajat.setAlignment(Element.ALIGN_CENTER);
                userSignatureParagraph.add(DateAndAngajat);
                Paragraph userName=new Paragraph(emp.getSurname()+" "+emp.getForname());
                documentParagraph.add(userSignatureParagraph);

                document.add(documentParagraph);
            }
            catch (Exception e){e.printStackTrace();}
        }
        document.add(new Paragraph(""));
        document.close();

       // emailingService.sendEmail("brianna.balaci@mhp.com",byteArrayOutputStream.toByteArray());
        holiday.setStatus(HolidayStatus.SENT);
        holidayRepository.save(holiday);
        return byteArrayOutputStream.toByteArray();

    }



    private User getTeamLeaderForUser(User user){
        Team team = teamRepository.getById(user.getTeam().getId());
        return userRepository.getById(team.getTeamLeader().getId());
    }


    private boolean checkRequestCreatedWhileTeamleadGone(DetailedHoliday d, Substitute s){
        log.info("holiday creation time: " + d.getCreationDate() + " teamlead holiday start date: " + s.getStartDate() + " end date " + s.getEndDate());
        if((d.getCreationDate().isAfter(s.getStartDate()) || d.getCreationDate().equals(s.getStartDate())) && (d.getCreationDate().equals(s.getEndDate()) || d.getCreationDate().isBefore(s.getEndDate())))
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
