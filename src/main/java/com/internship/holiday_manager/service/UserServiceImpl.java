package com.internship.holiday_manager.service;


import com.internship.holiday_manager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String authentication(String email, String password) {
        log.info("Login called");
        if(userRepository.findByEmailAndPassword(email, password) != null){
            log.info("Login called - SUCCESS");
            return "Logged in successfully!";
        }
        return "Failed to login! Wrong credentials!";
    }
}
