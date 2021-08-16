package com.mhp.planner.Services;

import com.mhp.planner.Dtos.DepartmentDto;
import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Dtos.UserPasswordDto;
import com.mhp.planner.Entities.Department;
import javassist.NotFoundException;

import javax.naming.AuthenticationException;
import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();
    List<UserDto> getByDepartment(String departmentName);
    UserDto findUser(UserPasswordDto userPasswordDto);
    UserDto createUser(UserDto userDto) throws NotFoundException;
    void deleteUser(Long id) throws NotFoundException;
    UserDto updateUser(UserDto userDto);
    UserDto changePasswordUser(UserPasswordDto userPasswordDto);
}
