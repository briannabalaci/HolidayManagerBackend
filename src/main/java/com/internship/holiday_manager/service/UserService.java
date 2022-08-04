package com.internship.holiday_manager.service;

import com.internship.holiday_manager.dto.ChangePasswordDto;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.User;

public interface UserService {

    User authentication(LoginUserDto dto);

    void changePassword(ChangePasswordDto dto);

    UserDto createUser(UserDto dto);

    /**
     * It checks if the new password is different from the old password and if the user with the given credentials exists or not
     * @param dto - It contains the email of the user, the old password and the new password
     * @return - it returns true if the new password is fine and the user with the given credentials exists, otherwise it returns false
     */
    boolean verifyPasswordAndCredentials(ChangePasswordDto dto);

}
