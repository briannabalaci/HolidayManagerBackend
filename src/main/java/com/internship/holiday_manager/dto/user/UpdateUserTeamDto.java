package com.internship.holiday_manager.dto.user;

import liquibase.change.DatabaseChangeNote;
import lombok.Data;

@Data
public class UpdateUserTeamDto {

    String email;
    String teamId;
}
