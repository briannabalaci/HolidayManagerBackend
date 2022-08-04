package com.internship.holiday_manager.service.user_service;

import com.internship.holiday_manager.dto.ChangePasswordDto;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.User;

public interface UserService {

    User authentication(LoginUserDto dto);

    String changePassword(ChangePasswordDto dto);
    UserDto createUser(UserDto dto);
}
