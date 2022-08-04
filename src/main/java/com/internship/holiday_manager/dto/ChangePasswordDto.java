package com.internship.holiday_manager.dto;

import lombok.Data;

@Data
public class ChangePasswordDto {

    private String email;

    private String oldPassword;

    private String newPassword;
}
