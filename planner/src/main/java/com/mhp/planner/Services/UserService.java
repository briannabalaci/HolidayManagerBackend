package com.mhp.planner.Services;

import com.mhp.planner.Dtos.UserDto;
import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();
    UserDto findUser(UserDto userDto);
    UserDto createUser(UserDto userDto);
}
