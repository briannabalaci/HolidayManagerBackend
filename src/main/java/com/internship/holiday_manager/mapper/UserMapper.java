package com.internship.holiday_manager.mapper;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.entity.User;
import org.mapstruct.*;
import java.util.List;


@Mapper(componentModel = "spring", uses = TeamMapper.class)
public interface UserMapper {

//    @Mapping(target = "team", ignore = true)
    UserDto entityToDto(User entity);

//    @Mapping(target = "team", ignore = true)
    User dtoToEntity(UserDto dto);

    List<UserDto> entitiesToDtos(List<User> entities);
    List<User> dtosToEntities(List<UserDto> dtos);
}



