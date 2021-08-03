package com.mhp.planner.Services;

import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Entities.User;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();
}
