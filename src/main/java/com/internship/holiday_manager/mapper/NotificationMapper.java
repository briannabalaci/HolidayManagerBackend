package com.internship.holiday_manager.mapper;

import com.internship.holiday_manager.dto.notification.NotificationDto;
import com.internship.holiday_manager.dto.team.TeamDto;
import com.internship.holiday_manager.entity.Notification;
import com.internship.holiday_manager.entity.Team;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserWithTeamIdMapper.class, HolidayMapper.class})
public interface NotificationMapper {

    NotificationDto entityToDto(Notification entity);

    Notification dtoToEntity(NotificationDto dto);

    List<NotificationDto> entitiesToDtos(List<Notification> entities);

    List<Notification> dtosToEntities(List<NotificationDto> dtos);
}
