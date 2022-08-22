package com.internship.holiday_manager.service.holiday_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class HolidayServiceImpl implements HolidayService{

    private final UserRepository userRepository;
    private final HolidayRepository holidayRepository;
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;
    private final HolidayMapper holidayMapper;

    public HolidayServiceImpl(UserRepository userRepository, HolidayRepository holidayRepository, TeamRepository teamRepository, NotificationService notificationService, HolidayMapper holidayMapper) {
        this.userRepository = userRepository;
        this.holidayRepository = holidayRepository;
        this.teamRepository = teamRepository;
        this.notificationService = notificationService;
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
        //if the user creating the requets is the teamlead, noHolidays will be substracted
        if(saved.getStatus() == HolidayStatus.APPROVED || saved.getStatus() == HolidayStatus.PENDING) {
            Integer noHolidays = getNoHolidays(saved.getStartDate(), saved.getEndDate());
            if(saved.getType() == HolidayType.REST) {
                updateUserNoHolidays(userRepository.getById(saved.getUser().getId()), noHolidays);
            } else if(saved.getType() == HolidayType.UNPAID){
                Integer noDays = this.getNoUnpaidDays(noHolidays);
                updateUserNoHolidays(userRepository.getById(saved.getUser().getId()), noDays);
            } else if(saved.getType() == HolidayType.SPECIAL){
                updateUserNoHolidays(userRepository.getById(saved.getUser().getId()), 0);
            }
        }

        HolidayDto savedHoliday = holidayMapper.entityToDto(saved);

        //send notification only if the user is part of a team
        UserWithTeamIdDto userDto = holidayDto.getUser();
        User user = userRepository.getById(userDto.getId());
        if(user.getTeam()!=null && saved.getStatus()== HolidayStatus.PENDING)
            sendNotificationToTeamLead(savedHoliday,NotificationType.SENT);
        
        return holidayMapper.entityToDto(saved);
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
    public HolidayDto updateHoliday(HolidayDto holidayDto) {
        Holiday u = holidayRepository.findByID(holidayDto.getId());
        u.setDetails(null);
        if(u!= null) {
//            if(u.getUser().getType() == UserType.EMPLOYEE)
//                sendNotificationToTeamLead(holidayMapper.entityToDto(u),NotificationType.UPDATE);
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
                getBackUserNoHolidays(holiday.getUser(), getNoHolidays(holiday.getStartDate(), holiday.getEndDate()));
            }
            holidayRepository.delete(holiday);
            return holidayMapper.entityToDto(holiday);
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
        User userToUpdate = userRepository.getById(holidayDto.getUser().getId());

        holidayDto.setStatus(HolidayStatus.APPROVED);
        holidayDto.setDetails(null);
        updated = this.updateHoliday(holidayDto);
        sendNotificationToEmployee(holidayDto,NotificationType.APPROVED);

        return updated;
    }

    @Override
    public HolidayDto denyHolidayRequest(Long id) {
        HolidayDto holidayDto = holidayMapper.entityToDto(holidayRepository.getById(id));
        holidayDto.setStatus(HolidayStatus.DENIED);
        holidayDto.setDetails(null);

        Integer noHolidays = this.getNoHolidays(holidayDto.getStartDate(), holidayDto.getEndDate());
        User user = this.userRepository.findById(holidayDto.getUser().getId()).get();

        if(holidayDto.getType() == HolidayType.REST){
            Integer newNoHolidays = noHolidays + user.getNrHolidays();
            getBackUserNoHolidays(user, newNoHolidays);
        } else if(holidayDto.getType() == HolidayType.UNPAID){
            Integer unpaidDays = getNoUnpaidDays(noHolidays);
            Integer newNoHolidays = noHolidays + unpaidDays;
            getBackUserNoHolidays(user, newNoHolidays);
        }

        sendNotificationToEmployee(holidayDto,NotificationType.DENIED);

        return this.updateHoliday(holidayDto);
    }

    @Override
    public HolidayDto requestMoreDetails(UpdateDetailsHolidayDto updateDetailsHolidayDto) {
        HolidayDto holidayDto = holidayMapper.entityToDto(holidayRepository.getById(updateDetailsHolidayDto.getId()));
        holidayDto.setDetails(updateDetailsHolidayDto.getDetails());

        sendNotificationToEmployee(holidayDto,NotificationType.MORE_DETAILS);

        return this.updateHoliday(holidayDto);
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
}
