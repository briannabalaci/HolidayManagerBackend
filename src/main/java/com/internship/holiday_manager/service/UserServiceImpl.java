package com.internship.holiday_manager.service;


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

    @Override
    public User authentication(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }
}
