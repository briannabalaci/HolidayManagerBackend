package com.mhp.planner.Dtos;

import lombok.Data;

import java.util.List;

@Data
public class UserDto {

    private Long id;

    private String forename;

    private String surname;

    private String email;

    private String password;

    private String role;

    private String department;

    private List<EventDto> events;

    private List<InvitesDto> invites;


}
