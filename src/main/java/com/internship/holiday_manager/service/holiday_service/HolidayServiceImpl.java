package com.internship.holiday_manager.service.holiday_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.holiday.HolidayTypeAndUserName;
import com.internship.holiday_manager.dto.holiday.UpdateDetailsHolidayDto;
import com.internship.holiday_manager.dto.notification.NotificationDto;
import com.internship.holiday_manager.dto.user.UserWithTeamIdDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import com.internship.holiday_manager.entity.enums.NotificationType;
import com.internship.holiday_manager.entity.enums.UserType;
import com.internship.holiday_manager.mapper.HolidayMapper;
import com.internship.holiday_manager.repository.HolidayRepository;
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

    public HolidayServiceImpl(UserRepository userRepository, HolidayRepository holidayRepository, TeamRepository teamRepository, NotificationService notificationService, TeamLeadService teamLeadService, HolidayMapper holidayMapper) {
        this.userRepository = userRepository;
        this.holidayRepository = holidayRepository;
        this.teamRepository = teamRepository;
        this.notificationService = notificationService;
        this.teamLeadService = teamLeadService;
        this.holidayMapper = holidayMapper;
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

    @Override
    public HolidayDto createHoliday(HolidayDto holidayDto) {
        HolidayDto updatedHolidayDto = this.setStatusHoliday(holidayDto);

        Holiday entityToSave = holidayMapper.dtoToEntity(updatedHolidayDto);
        Holiday saved = holidayRepository.save(entityToSave);

        if(saved.getStatus() == HolidayStatus.APPROVED || saved.getStatus() == HolidayStatus.PENDING) {
            this.decreaseNoHolidays(saved);
        }

        HolidayDto savedHoliday = holidayMapper.entityToDto(saved);

        //send notification only if the user is part of a team
        UserWithTeamIdDto userDto = holidayDto.getUser();
        User user = userRepository.getById(userDto.getId());
        if(user.getTeam()!=null && saved.getStatus()== HolidayStatus.PENDING)
            sendNotificationToTeamLead(savedHoliday,NotificationType.SENT);

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
        if(dto.getType()==null && dto.getForname()!=null && dto.getSurname()!=null)
            return holidayMapper.entitiesToDtos(holidayRepository.filterByUserName(dto.getTeamLeaderId(), dto.getForname(), dto.getSurname()));
        else if(dto.getType()==null && dto.getForname()==null && dto.getSurname()!=null)
            return holidayMapper.entitiesToDtos(holidayRepository.filterByOneUserName(dto.getTeamLeaderId(), dto.getSurname()));
        else if(dto.getType()==null && dto.getForname()!=null && dto.getSurname()==null)
            return holidayMapper.entitiesToDtos(holidayRepository.filterByOneUserName(dto.getTeamLeaderId(), dto.getForname()));
        else if(dto.getType()!=null && dto.getForname()==null && dto.getSurname()!=null)
            return holidayMapper.entitiesToDtos(holidayRepository.filterByTypeAndOneUserName(dto.getTeamLeaderId(), dto.getType(), dto.getSurname()));
        else if(dto.getType()!=null && dto.getForname()!=null && dto.getSurname()==null)
            return holidayMapper.entitiesToDtos(holidayRepository.filterByTypeAndOneUserName(dto.getTeamLeaderId(), dto.getType(), dto.getForname()));
        else if(dto.getType()!=null && dto.getForname()==null && dto.getForname()==null)
            return holidayMapper.entitiesToDtos(holidayRepository.filterByType(dto.getTeamLeaderId(), dto.getType()));
        else if(dto.getType()!=null && dto.getForname()!=null && dto.getForname()!=null)
            return holidayMapper.entitiesToDtos(holidayRepository.filterByTypeAndUserName(dto.getTeamLeaderId(), dto.getType(), dto.getForname(), dto.getSurname()));

        return teamLeadService.getTeamRequests(userRepository.getById(dto.getTeamLeaderId()).getTeam().getId());

    }

    @Override
    public List<HolidayDto> filterByType(Long teamLeaderId, HolidayType type) {
        return holidayMapper.entitiesToDtos(holidayRepository.filterByType(teamLeaderId, type));
    }

    @Override
    public List<HolidayDto> filterByUserName(Long teamLeaderId, String forname, String surname) {
        return holidayMapper.entitiesToDtos(holidayRepository.filterByUserName(teamLeaderId, forname, surname));
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

        List<Holiday> hds = holidayRepository.findByUserId(user.getId()).stream().filter(x -> !x.getId().equals(holidayId)).collect(Collectors.toList());

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
