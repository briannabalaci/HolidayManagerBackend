package com.mhp.planner.Dtos;

import com.mhp.planner.Entities.Event;
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


}
