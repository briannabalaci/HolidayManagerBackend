package com.mhp.planner.Mappers;

import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, DepartmentMapper.class, EventMapper.class})
public interface UserMapper {

    @Mapping(target = "role", expression = "java( user.getRole().getName())")
    @Mapping(target = "department", expression = "java( user.getDepartment().getName())")
    UserDto entity2Dto (User user);

//    @Mapping(target = "role", expression = "java( new Role(userDto.getId(),userDto.getName()))")
//    @Mapping(target = "department", expression = "java( new Department(userDto.getId(),userDto.getName()))")
//    User dto2entity (UserDto userDto);


    List<UserDto> entities2dtos(List<User> userList);


//    List<User> dtos2entities(List<UserDto> userDtos);
}
