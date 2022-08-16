package com.internship.holiday_manager.dto.team;

import com.internship.holiday_manager.dto.user.UserWithTeamIdDto;
import lombok.Data;

import java.util.List;


@Data
public class TeamDto {
    private Long id;
    private String name;
    private UserWithTeamIdDto teamLeader;
    private List<UserWithTeamIdDto> members;
}

