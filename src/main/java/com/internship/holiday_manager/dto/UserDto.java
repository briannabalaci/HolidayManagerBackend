package com.internship.holiday_manager.dto;
import com.internship.holiday_manager.dto.TeamDto;
import com.internship.holiday_manager.entity.Team;
import com.internship.holiday_manager.entity.enums.Department;
import com.internship.holiday_manager.entity.enums.Role;
import com.internship.holiday_manager.entity.enums.UserType;
import lombok.Data;

import java.util.List;
@Data
public class UserDto {
    private Long id;
    private String email;
    private String password;
    private String forname;
    private String surname;
    private Department department;
    private Role role;
    private Integer nrHolidays;
    private UserType type;
    private TeamDto team;
    //private List<HolidayDto> holidays;
}



