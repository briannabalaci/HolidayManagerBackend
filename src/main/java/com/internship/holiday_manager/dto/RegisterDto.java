package com.internship.holiday_manager.dto;

import com.internship.holiday_manager.entity.enums.Department;
import com.internship.holiday_manager.entity.enums.Role;
import com.internship.holiday_manager.entity.enums.UserType;
import lombok.Data;

@Data
public class RegisterDto {
    private String email;
    private String password;
    private String forname;
    private String surname;
    private Department department;
    private Role role;
    private Integer nrHolidays;
    private UserType type;
}
