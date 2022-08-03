package com.internship.holiday_manager.service;


import com.internship.holiday_manager.dto.ChangePasswordDto;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.internship.holiday_manager.entity.User;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User authentication(LoginUserDto dto) {
        return userRepository.findByEmailAndPassword(dto.getEmail(), dto.getPassword());
    }

    @Override
    public String changePassword(ChangePasswordDto dto) {
        User u = userRepository.findByEmailAndPassword(dto.getEmail(), dto.getOldPassword());
        if(u != null){
            return "Password updated successfully!";
        }
        return "Email or password incorrect!";
    }
}
