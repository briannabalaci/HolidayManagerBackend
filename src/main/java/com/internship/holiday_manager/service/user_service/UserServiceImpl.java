package com.internship.holiday_manager.service.user_service;


import com.internship.holiday_manager.dto.ChangePasswordDto;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.dto.UpdateUserDto;
import com.internship.holiday_manager.dto.UserDto;

import com.internship.holiday_manager.mapper.UserMapper;
import com.internship.holiday_manager.repository.UserRepository;
import com.internship.holiday_manager.service.user_service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.internship.holiday_manager.entity.User;

import java.util.List;
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
    

    public UserDto authentication(LoginUserDto dto) {
        return userMapper.entityToDto(userRepository.findByEmailAndPassword(dto.getEmail(), dto.getPassword()));
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
    public List<UserDto> getAll() {
      List<User> entities = userRepository.findAll();
      List<UserDto> dtos= userMapper.entitiesToDtos(entities);
      return  dtos;
    }


    @Override
    public boolean verifyPasswordAndCredentials(ChangePasswordDto dto) {
        if(Objects.equals(dto.getOldPassword(), dto.getNewPassword())){
           return false;
        }

        return userRepository.findByEmailAndPassword(dto.getEmail(), dto.getOldPassword()) != null;
    }

    @Override
    public UserDto updateUser(UpdateUserDto dto){
        User u = userRepository.findByEmail(dto.getEmail());
        if(u!= null) {
            u.setPassword(dto.getPassword());
            u.setForname(dto.getForname());
            u.setSurname(dto.getSurname());
            u.setDepartment(dto.getDepartment());
            u.setNrHolidays(dto.getNrHolidays());
            u.setRole((dto.getRole()));
        }
        return userMapper.entityToDto(userRepository.save(u));
    }

    @Override
    public void deleteUser(String email) {
        User u = userRepository.findByEmail(email);
        if(u != null) {
            userRepository.deleteById(u.getId());
        }
    }

}
