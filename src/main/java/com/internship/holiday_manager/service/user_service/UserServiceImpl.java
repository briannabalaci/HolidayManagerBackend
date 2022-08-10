package com.internship.holiday_manager.service.user_service;

import com.internship.holiday_manager.dto.*;

import com.internship.holiday_manager.mapper.UserMapper;
import com.internship.holiday_manager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.internship.holiday_manager.entity.User;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    public UserDto authentication(LoginUserDto dto) {
        return userMapper.entityToDto(userRepository.findByEmailAndPassword(dto.getEmail(), dto.getPassword()));
    }

    public User getUserInformation(LoginUserDto dto) {
        User user = userRepository.findByEmail(dto.getEmail());
        if (user != null) {
            if (passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                return user;
            else return null;
        }
        return userRepository.findByEmailAndPassword(dto.getEmail(), dto.getPassword());

    }

    @Override
    public void changePassword(ChangePasswordDto dto) {
        User u = userRepository.findByEmail(dto.getEmail());
        u.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(u);
    }

    @Override
    public UserDto createUser(RegisterDto dto) {
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        User user = User.builder().email(dto.getEmail())
                                    .password(dto.getPassword())
                                    .surname(dto.getSurname())
                                    .forname(dto.getForname())
                                    .nrHolidays(dto.getNrHolidays())
                                    .department(dto.getDepartment())
                                    .type(dto.getType())
                                    .role(dto.getRole())
                                    .build();
        User savedUser = userRepository.save(user);
        return userMapper.entityToDto(savedUser);
    }

    @Override
    public List<UserDto> getAll() {
        List<User> entities = userRepository.findAll();
        return userMapper.entitiesToDtos(entities);
    }


    @Override
    public boolean verifyPasswordAndCredentials(ChangePasswordDto dto) {
        if (Objects.equals(dto.getOldPassword(), dto.getNewPassword())) {
            return false;
        }
        User user = userRepository.findByEmail(dto.getEmail());
        if (user != null) {
            return passwordEncoder.matches(dto.getOldPassword(), user.getPassword());
        } else return false;
    }

    @Override
    public boolean userExists(RegisterDto dto) {
        User user = userRepository.findByEmail(dto.getEmail());
        if(user != null) {
            return true;
        } else {
            return false;
        }
    }


    private void ChangeUserData(UpdateUserDto dto, User u) {
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setForname(dto.getForname());
        u.setSurname(dto.getSurname());
        u.setDepartment(dto.getDepartment());
        u.setNrHolidays(dto.getNrHolidays());
        u.setRole((dto.getRole()));
    }
    private void ChangeUserDataWithoutPassword(UpdateUserDto dto, User u) {
        u.setForname(dto.getForname());
        u.setSurname(dto.getSurname());
        u.setDepartment(dto.getDepartment());
        u.setNrHolidays(dto.getNrHolidays());
        u.setRole((dto.getRole()));
    }

    @Override
    public UserDto updateUser(UpdateUserDto dto) {
        User u = userRepository.findByEmail(dto.getEmail());
        if (u != null) {
            if(dto.getPassword() != null){
                ChangeUserData(dto, u);
            }
            else {
                ChangeUserDataWithoutPassword(dto,u);
            }

        }
        return userMapper.entityToDto(userRepository.save(u));
    }

    @Override
    public void deleteUser(String email) {
        User u = userRepository.findByEmail(email);
        if (u != null) {
            userRepository.deleteById(u.getId());
        }
    }

    @Override
    public UserDto getUser(String email) {
        return userMapper.entityToDto(this.userRepository.findByEmail(email));
    }

}
