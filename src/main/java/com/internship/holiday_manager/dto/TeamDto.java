package com.internship.holiday_manager.dto;

import lombok.Data;

import java.util.List;


@Data
public class TeamDto {
    private Long id;
    private String name;
    private UserDto teamLeader;
    private List<UserDto> members;
}

