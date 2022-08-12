package com.internship.holiday_manager.dto.user;

import com.internship.holiday_manager.entity.enums.Department;
import com.internship.holiday_manager.entity.enums.Role;
import lombok.Data;

@Data
public class UpdateUserDto {

    private String email;
    private String password;
    private String forname;
    private String surname;
    private Department department;
    private Role role;
    private Integer nrHolidays;

}
