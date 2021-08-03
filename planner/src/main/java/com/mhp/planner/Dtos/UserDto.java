package com.mhp.planner.Dtos;

import com.mhp.planner.Entities.Department;
import com.mhp.planner.Entities.Role;
import lombok.Data;

@Data
public class UserDto {

    private Long id;

    private String forename;

    private String surname;

    private String email;

    private String password;

    private RoleDto role_fk;

    private DepartmentDto department_fk;



}
