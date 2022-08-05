package com.internship.holiday_manager.service.user_service;


import com.internship.holiday_manager.dto.ChangePasswordDto;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.dto.UserDto;

import com.internship.holiday_manager.mapper.UserMapper;
import com.internship.holiday_manager.repository.UserRepository;
import com.internship.holiday_manager.service.user_service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.internship.holiday_manager.entity.User;

import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
    
    public User getUserInformation(LoginUserDto dto) {
        return userRepository.findByEmailAndPassword(dto.getEmail(), dto.getPassword());
    }

    @Override
    public void changePassword(ChangePasswordDto dto) {
        User u = userRepository.findByEmailAndPassword(dto.getEmail(), dto.getOldPassword());
        u.setPassword(dto.getNewPassword());
        userRepository.save(u);
    }
   @Override
    public UserDto createUser(UserDto dto){
        User user=userRepository.save(userMapper.dtoToEntity(dto));
        return userMapper.entityToDto(user);
    }


    @Override
    public boolean verifyPasswordAndCredentials(ChangePasswordDto dto) {
        if(Objects.equals(dto.getOldPassword(), dto.getNewPassword())){
           return false;
        }

        return userRepository.findByEmailAndPassword(dto.getEmail(), dto.getOldPassword()) != null;
    }
}
