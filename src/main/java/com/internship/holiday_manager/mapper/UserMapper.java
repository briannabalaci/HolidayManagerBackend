package com.internship.holiday_manager.mapper;

import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto entityToDto(User entity);

    User dtoToEntity(UserDto dto);

    List<UserDto> entitiesToDtos(List<User> entities);

    List<User> dtosToEntities(List<UserDto> dtos);
}
