package com.mhp.planner.Mappers;


import com.mhp.planner.Dtos.RoleDto;
import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Entities.Role;
import com.mhp.planner.Entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {


    RoleDto entity2Dto (Role role);

    Role dto2entity (RoleDto episodeDto);

    List<RoleDto> entities2dtos(List<Role> userList);

    List<Role> dtos2entities(List<RoleDto> userDtos);
}
