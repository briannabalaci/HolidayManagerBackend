package com.internship.holiday_manager.dto;

import liquibase.change.DatabaseChangeNote;
import lombok.Data;

@Data
public class UpdateUserTeamDto {

    String email;
    String teamId;
}
