package com.internship.holiday_manager.service.user_service;

import com.internship.holiday_manager.dto.*;
import com.internship.holiday_manager.entity.User;

import java.util.List;

public interface UserService {

    /**
     * It returns UserDto for the user that matches LoginUserDto
     * @param dto contains user login data
     * @return UserDto for the user that matches LoginUserDto data. If no user is found it return null
     */
    UserDto authentication(LoginUserDto dto);
    User getUserInformation(LoginUserDto dto);

    void changePassword(ChangePasswordDto dto);

    UserDto createUser(RegisterDto dto);
    List<UserDto> getAll();
    /**
     * It checks if the new password is different from the old password and if the user with the given credentials exists or not
     * @param dto - It contains the email of the user, the old password and the new password
     * @return - it returns true if the new password is fine and the user with the given credentials exists, otherwise it returns false
     */
    boolean verifyPasswordAndCredentials(ChangePasswordDto dto);
    boolean userExists(RegisterDto dto);
    UserDto updateUser(UpdateUserDto dto);

    void deleteUser(String email);

}
