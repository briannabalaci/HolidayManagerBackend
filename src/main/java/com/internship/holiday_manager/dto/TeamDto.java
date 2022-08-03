package com.internship.holiday_manager.dto;

import lombok.Data;

import javax.persistence.Column;

@Data
public class TeamDto {
    private Long id;

    private String name;

    private String teamLeader;


}
