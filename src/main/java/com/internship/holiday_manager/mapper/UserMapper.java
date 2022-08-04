package com.internship.holiday_manager.mapper;

import com.internship.holiday_manager.dto.TeamDto;
import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.Team;
import com.internship.holiday_manager.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = TeamMapper.class)
public interface UserMapper {

    UserDto entityToDto(User entity);

    User dtoToEntity(UserDto dto);

    List<UserDto> entitiesToDtos(List<User> entities);

    List<User> dtosToEntities(List<UserDto> dtos);
}
