package com.internship.holiday_manager.dto.substitute;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.dto.user.UserWithTeamIdDto;
import com.internship.holiday_manager.entity.User;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
public class SubstituteDto {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UserDto substitute;
    private UserDto teamLead;
}
