package com.internship.holiday_manager.dto.team;

import lombok.Data;

import java.util.List;

@Data
public class TeamAddDto {
    private String name;
    private Long teamLeaderId;
    private List<Long> membersId;
}
