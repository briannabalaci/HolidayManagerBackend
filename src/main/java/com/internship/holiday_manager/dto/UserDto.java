package com.internship.holiday_manager.dto;

import com.internship.holiday_manager.entity.enums.Department;
import com.internship.holiday_manager.entity.enums.Role;
import com.internship.holiday_manager.entity.enums.UserType;
import lombok.Data;

@Data
public class UserDto {

    private Long id;
    private String email;
    private String password;
    private String forname;
    private String surname;
    private String department;
    private String role;
    private Integer nrHolidays;
    private String type;
    private Long teamId;
}
