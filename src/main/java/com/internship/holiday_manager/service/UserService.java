package com.internship.holiday_manager.service;

import com.internship.holiday_manager.dto.ChangePasswordDto;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.User;
import liquibase.change.Change;

public interface UserService {

    User authentication(LoginUserDto dto);

    void changePassword(ChangePasswordDto dto);

    UserDto createUser(UserDto dto);

    boolean verifyPassword(ChangePasswordDto dto);

}
