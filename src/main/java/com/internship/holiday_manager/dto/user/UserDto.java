package com.internship.holiday_manager.dto.user;
import com.internship.holiday_manager.dto.team.TeamDto;
import com.internship.holiday_manager.entity.enums.Department;
import com.internship.holiday_manager.entity.enums.Role;
import com.internship.holiday_manager.entity.enums.UserType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String forname;
    private String surname;
    private Department department;
    private Role role;
    private Integer nrHolidays;
    private UserType type;
    private TeamDto team;

}



