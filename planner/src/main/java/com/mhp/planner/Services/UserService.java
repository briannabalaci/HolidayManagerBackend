package com.mhp.planner.Services;

import com.mhp.planner.Dtos.UserDto;
import javassist.NotFoundException;

import javax.naming.AuthenticationException;
import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();
    UserDto findUser(UserDto userDto);
    UserDto createUser(UserDto userDto) throws NotFoundException;
    void deleteUser(Long id) throws NotFoundException;
    UserDto updateUser(UserDto userDto);
    UserDto changePasswordUser(UserDto userDto);
}
