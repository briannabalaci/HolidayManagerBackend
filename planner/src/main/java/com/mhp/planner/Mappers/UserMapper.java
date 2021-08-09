package com.mhp.planner.Mappers;

import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Entities.User;
import com.mhp.planner.Repository.DepartmentRepository;
import com.mhp.planner.Repository.RoleRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class,DepartmentMapper.class, RoleRepository.class, DepartmentRepository.class})
public interface UserMapper {

    @Mapping(target = "role", expression = "java( user.getRole().getName())")
    @Mapping(target = "department", expression = "java( user.getDepartment().getName())")
    UserDto entity2Dto (User user);

    List<UserDto> entities2dtos(List<User> userList);

    User dto2entity(UserDto userDto);

//    List<User> dtos2entities(List<UserDto> userDtos);
}
