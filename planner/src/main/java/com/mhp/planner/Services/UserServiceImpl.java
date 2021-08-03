package com.mhp.planner.Services;


import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Entities.User;
import com.mhp.planner.Mappers.UserMapper;
import com.mhp.planner.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        List<User> userList = userRepository.findAll();

        System.out.println(userList);

        return userMapper.entities2dtos(userList);


    }
}
