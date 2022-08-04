package com.internship.holiday_manager.service;


import com.internship.holiday_manager.dto.ChangePasswordDto;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.dto.UserDto;

import com.internship.holiday_manager.mapper.UserMapper;
import com.internship.holiday_manager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.internship.holiday_manager.entity.User;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
    
    public User authentication(LoginUserDto dto) {
        return userRepository.findByEmailAndPassword(dto.getEmail(), dto.getPassword());
    }

    @Override
    public String changePassword(ChangePasswordDto dto) {
        User u = userRepository.findByEmailAndPassword(dto.getEmail(), dto.getOldPassword());
        if(u != null){
            u.setPassword(dto.getNewPassword());
            userRepository.save(u);
            return "Password updated successfully!";
        }
        return "Email or password incorrect!";
    }
   @Override
    public UserDto createUser(UserDto dto){
        User user=userRepository.save(userMapper.dtoToEntity(dto));
        return userMapper.entityToDto(user);

    }
}
