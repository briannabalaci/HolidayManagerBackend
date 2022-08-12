package com.internship.holiday_manager.mapper;

import com.internship.holiday_manager.dto.user.LoginUserDto;
import com.internship.holiday_manager.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoginUserMapper {

    LoginUserDto entityToDto(User entity);

    User dtoToEntity(LoginUserDto dto);

    List<LoginUserDto> entitiesToDtos(List<User> entities);

    List<User> dtosToEntities(List<LoginUserDto> dtos);

}
