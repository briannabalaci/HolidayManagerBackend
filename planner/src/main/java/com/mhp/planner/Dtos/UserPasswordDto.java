package com.mhp.planner.Dtos;

import lombok.Data;

@Data
public class UserPasswordDto {
    private Long id;

    private String email;

    private String password;

    private String newPassword;

}
