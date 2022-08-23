package com.internship.holiday_manager.service.user_service;

import com.internship.holiday_manager.dto.*;
import com.internship.holiday_manager.dto.user.LoginUserDto;
import com.internship.holiday_manager.dto.user.UpdateUserDto;
import com.internship.holiday_manager.dto.user.UserDto;
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

    /**
     * This method updates the data about the user
     * @param dto - contains the data about the user we want to update
     * @return - the updated user
     */
    UserDto updateUser(UpdateUserDto dto);

    /**
     * This method deletes a user by its email
     * @param email - the email of the user which is unique
     */
    void deleteUser(String email);


    UserDto findUserById(Long id);

    /**
     * This method finds the user information based on its email
     * @param email - the email of the user
     * @return - the user it searched for
     */
    UserDto getUser(String email);
    public List<UserDto> getUsersWithoutTeam();

    /**
     * Finds the user based on its id
     * @param id - the id after which we search
     * @return - the user
     */
    UserDto getUserById(Long id);

    /**
     * We get the number of vacations days for the user
     * @param id - the email of the user
     * @return - the number of vacations days
     */
    Integer getNoHolidaysUser(Long id);


    /**
     * We update the number of vacation days of the user
     * @param email - the email of the user
     * @param noDays - the new number of vacation days
     * @return - the updated user
     */
    UserDto updateNoHolidaysUser(String email, Integer noDays);

}
