package com.internship.holiday_manager.mapper;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.substitute.SubstituteDto;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.Substitute;
import org.mapstruct.Mapper;

import java.util.List;
@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface SubstituteMapper {

    SubstituteDto entityToDto(Substitute entity);

    Substitute dtoToEntity(SubstituteDto dto);

    List<SubstituteDto> entitiesToDtos(List<Substitute> entities);

    List<Substitute> dtosToEntities(List<SubstituteDto> dtos);
}
