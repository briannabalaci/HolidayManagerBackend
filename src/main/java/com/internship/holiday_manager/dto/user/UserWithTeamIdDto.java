package com.internship.holiday_manager.dto.user;

import com.internship.holiday_manager.entity.enums.Department;
import com.internship.holiday_manager.entity.enums.Role;
import com.internship.holiday_manager.entity.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithTeamIdDto {
    private Long id;
    private String email;
    private String forname;
    private String surname;
    private Department department;
    private Role role;
    private Integer nrHolidays;
    private UserType type;
    private Long teamId;
}
