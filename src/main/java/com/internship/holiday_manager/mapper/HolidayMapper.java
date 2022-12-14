package com.internship.holiday_manager.mapper;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.entity.Holiday;
import org.mapstruct.Mapper;

import java.util.List;
@Mapper(componentModel = "spring", uses = UserWithTeamIdMapper.class)
public interface HolidayMapper {

    HolidayDto entityToDto(Holiday entity);

    Holiday dtoToEntity(HolidayDto dto);

    List<HolidayDto> entitiesToDtos(List<Holiday> entities);

    List<Holiday> dtosToEntities(List<HolidayDto> dtos);
}
