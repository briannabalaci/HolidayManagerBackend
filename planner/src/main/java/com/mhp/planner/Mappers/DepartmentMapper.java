package com.mhp.planner.Mappers;


import com.mhp.planner.Dtos.DepartmentDto;
import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Entities.Department;
import com.mhp.planner.Entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "id", ignore = true)
    DepartmentDto entity2Dto (Department episode);

    Department dto2entity (DepartmentDto episodeDto);

    List<DepartmentDto> entities2dtos(List<Department> userList);

    List<Department> dtos2entities(List<DepartmentDto> userDtos);
}
