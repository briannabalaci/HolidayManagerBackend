package com.mhp.planner.Mappers;

import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class,DepartmentMapper.class})
public interface UserMapper {

    UserDto entity2Dto (User episode);


    User dto2entity (UserDto episodeDto);


    List<UserDto> entities2dtos(List<User> userList);


    List<User> dtos2entities(List<UserDto> userDtos);
}
