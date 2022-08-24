package com.internship.holiday_manager.service.holiday_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.holiday.HolidayTypeAndUserName;
import com.internship.holiday_manager.dto.holiday.UpdateDetailsHolidayDto;
import com.internship.holiday_manager.dto.notification.NotificationDto;
import com.internship.holiday_manager.dto.user.UserWithTeamIdDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.Substitute;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import com.internship.holiday_manager.entity.enums.NotificationType;
import com.internship.holiday_manager.entity.enums.UserType;
import com.internship.holiday_manager.mapper.HolidayMapper;
import com.internship.holiday_manager.repository.HolidayRepository;
import com.internship.holiday_manager.repository.SubstituteRepository;
import com.internship.holiday_manager.repository.TeamRepository;
import com.internship.holiday_manager.repository.UserRepository;
import com.internship.holiday_manager.service.notification_service.NotificationService;
import com.internship.holiday_manager.service.teamlead_service.TeamLeadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HolidayServiceImpl implements HolidayService{

    private final UserRepository userRepository;
    private final HolidayRepository holidayRepository;
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;
    private final TeamLeadService teamLeadService;
    private final HolidayMapper holidayMapper;

    private final SubstituteRepository substituteRepository;

    public HolidayServiceImpl(TeamLeadService teamLeadService, UserRepository userRepository, HolidayRepository holidayRepository, TeamRepository teamRepository, NotificationService notificationService, HolidayMapper holidayMapper, SubstituteRepository substituteRepository) {

        this.userRepository = userRepository;
        this.holidayRepository = holidayRepository;
        this.teamRepository = teamRepository;
        this.notificationService = notificationService;
        this.teamLeadService = teamLeadService;
        this.holidayMapper = holidayMapper;
        this.substituteRepository = substituteRepository;
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

        LocalDateTime dateTime = LocalDateTime.now();

        List<Holiday> holidays = requests.stream()
                .filter(h -> h.getStartDate().isBefore(dateTime) || h.getStartDate() == dateTime)
                .collect(Collectors.toList());

        return holidays.size() != 0;
    }

    private void createReplacement(User owner, HolidayDto request, Long substituteId){
        User substitute = userRepository.findById(substituteId).get();
        Substitute replacement = Substitute.builder()
                .substitute(substitute)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .teamLead(owner).build();

        this.substituteRepository.save(replacement);
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
        if(user.getType() == UserType.TEAMLEAD){
            createReplacement(user, holidayDto, substituteId);
        }

        HolidayDto savedHoliday = holidayMapper.entityToDto(saved);

        //send notification only if the user is part of a team
        if(user.getTeam()!=null && saved.getStatus()== HolidayStatus.PENDING) {

            User teamLeader = user.getTeam().getTeamLeader();
            User substitute = this.userRepository.getById(substituteId);

            if (!isTeamLeadInHoliday(teamLeader))
                sendNotificationToTeamLead(savedHoliday, NotificationType.SENT);
            else
                sendNotificationToSubstitute(savedHoliday, NotificationType.SENT, substitute);
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
    public HolidayDto updateHolidayRequest(HolidayDto holidayDto) {
        Holiday holiday = holidayRepository.findByID(holidayDto.getId());
        User user = userRepository.findById(holiday.getUser().getId()).get();
        if(user.getType() == UserType.TEAMLEAD){
            holidayDto.setStatus(HolidayStatus.APPROVED);
        }
        else {
            holidayDto.setStatus(HolidayStatus.PENDING);
        }

        return this.updateHoliday(holidayDto);
    }


    @Override
    public HolidayDto updateHoliday(HolidayDto holidayDto) {
        Holiday u = holidayRepository.findByID(holidayDto.getId());
        u.setDetails(null);
        if(u!= null) {
            if(u.getUser().getType() == UserType.EMPLOYEE )
                sendNotificationToTeamLead(holidayMapper.entityToDto(u),NotificationType.UPDATE);
            ChangeHolidayData(holidayDto,u);
        }
        return holidayMapper.entityToDto(holidayRepository.save(u));
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

    @Override
    public HolidayDto deleteHoliday(Long id) {
        Holiday holiday = holidayRepository.findByID(id);
        if (holiday != null) {
            if ( holiday.getStatus().equals(HolidayStatus.APPROVED) || holiday.getStatus().equals(HolidayStatus.PENDING)){
                this.increaseNoHolidays(holiday);
            }
            holidayRepository.delete(holiday);
            HolidayDto holidayDto = holidayMapper.entityToDto(holiday);

            //send notification
            User sender = userRepository.getById(holidayDto.getUser().getId()); // the user that made the holiday request
            User receiver = teamRepository.getById(sender.getTeam().getId()).getTeamLeader();
            UserWithTeamIdDto receiverDto = UserWithTeamIdDto.builder()
                    .id(receiver.getId()).email(receiver.getEmail()).forname(receiver.getForname()).surname(receiver.getSurname()).department(receiver.getDepartment())
                    .role(receiver.getRole()).nrHolidays(receiver.getNrHolidays()).type(receiver.getType()).teamId(receiver.getTeam().getId())
                    .build();

            NotificationDto notificationDto = new NotificationDto();
            notificationDto.setReceiver(receiverDto);
            notificationDto.setSender(holidayDto.getUser());
            notificationDto.setType(NotificationType.CANCELED);
            notificationDto.setSendDate(LocalDateTime.now());
            notificationDto.setSeen(false);
            notificationDto.setRequest(null);

            notificationService.createNotification(notificationDto);
            return holidayDto;
        }
        return null;
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
        notificationDto.setSendDate(LocalDateTime.now());
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
        notificationDto.setSendDate(LocalDateTime.now());
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
        notificationDto.setSendDate(LocalDateTime.now());
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

        List<Holiday> hds = holidayRepository.findUsersHolidays(user.getId()).stream().filter(x -> x.getId()!=holidayId).collect(Collectors.toList());
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

}
