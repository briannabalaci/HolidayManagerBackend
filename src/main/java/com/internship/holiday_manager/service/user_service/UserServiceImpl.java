package com.internship.holiday_manager.service.user_service;

import com.internship.holiday_manager.dto.*;
import com.internship.holiday_manager.dto.user.LoginUserDto;
import com.internship.holiday_manager.dto.user.UpdateUserDto;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.dto.user.UserNameDto;

import com.internship.holiday_manager.entity.*;
import com.internship.holiday_manager.entity.enums.UserType;
import com.internship.holiday_manager.mapper.UserMapper;
import com.internship.holiday_manager.mapper.UserWithTeamIdMapper;
import com.internship.holiday_manager.repository.*;

import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.repository.HolidayRepository;
import com.internship.holiday_manager.repository.NotificationRepository;
import com.internship.holiday_manager.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private final UserWithTeamIdMapper userWithTeamIdMapper;

    @Autowired
    private final NotificationRepository notificationRepository;

    @Autowired
    private final SubstituteRepository substituteRepository;

    @Autowired
    private final HolidayRepository holidayRepository;

    @Autowired
    private final DetailedHolidayRepository detailedHolidayRepository;


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
        return user != null;
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
            if (dto.getPassword() != null) {
                ChangeUserData(dto, u);
            } else {
                ChangeUserDataWithoutPassword(dto, u);
            }

        }
        return userMapper.entityToDto(userRepository.save(u));
    }

    @Override
    public void deleteUser(String email) {
        User u = userRepository.findByEmail(email);

        this.notificationRepository.deleteAll(this.notificationRepository.findByReceiver(u));
        this.notificationRepository.deleteAll(this.notificationRepository.findBySender(u));

        List<Holiday> holidays = this.holidayRepository.findByUserId(u.getId());

        holidays.forEach(h ->
        {
            this.detailedHolidayRepository.delete(this.detailedHolidayRepository.findByHoliday(h));
        });

        this.holidayRepository.deleteAll(holidays);

        if(u.getType().equals(UserType.TEAMLEAD)){
            this.substituteRepository.deleteAll(this.substituteRepository.findByTeamLead(u));

            List<Substitute> substitutes = this.substituteRepository.findBySubstitute(u);
            substitutes.forEach(s -> s.setSubstitute(null));
        }

        if (u != null) {
            userRepository.deleteById(u.getId());
        }
    }


    @Override
    public UserDto findUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (!user.isEmpty()) {
            UserDto userDto = userMapper.entityToDto(user.get());
            return userDto;
        } else return null;
    }

    //TODO: De vazut daca mai e nevoie de metoda sau nu
//    @Override
//    public UserWithTeamIdDto getUser(String email) {
//        User u = this.userRepository.findByEmail(email);
//        UserWithTeamIdDto dto = userWithTeamIdMapper.entityToDto(u);
//        dto.setTeamId(u.getTeam().getId());
//        return dto;
//    }

    @Override
    public UserDto getUser(String email) {
        User user = this.userRepository.findByEmail(email);
        return userMapper.entityToDto(user);

    }

    public List<UserDto> getUsersWithoutTeam() {
        List<UserDto> dtos = new ArrayList<>();
        for (User u : userRepository.findByTeamIsNullAndTypeNot(UserType.ADMIN)) {
            UserDto user = UserDto.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .surname(u.getSurname())
                    .forname(u.getForname())
                    .nrHolidays(u.getNrHolidays())
                    .department(u.getDepartment())
                    .type(u.getType())
                    .role(u.getRole())
                    .team(null)
                    .build();
            dtos.add(user);
        }
        return dtos;
    }

    @Override
    public UserDto getUserById(Long id) {
        Optional<User> user = this.userRepository.findById(id);
        return this.userMapper.entityToDto(user.get());
    }


    @Override
    public Integer getNoHolidaysUser(Long id) {
        User user = this.userRepository.findById(id).get();
        return user.getNrHolidays();
    }

    @Override
    public UserDto updateNoHolidaysUser(String email, Integer noDays) {
        User user = this.userRepository.findByEmail(email);

        user.setNrHolidays(noDays);

        User newUser = this.userRepository.save(user);

        return this.userMapper.entityToDto(newUser);
    }

    @Override
    public List<UserDto> filterByName(UserNameDto userNameDto) {
        return userMapper.entitiesToDtos(userRepository.filterByName(userNameDto.getForname(), userNameDto.getSurname()));
    }

    @Override
    public List<UserDto> getAllUsersWithoutTeamLead(Long teamLeadId) {
        List<User> users = this.userRepository.findAll();

        List<User> usersWithoutTeamLead = users.stream()
                .filter(user ->!Objects.equals(user.getId(), teamLeadId) && user.getType() == UserType.TEAMLEAD)
                .collect(Collectors.toList());

        return userMapper.entitiesToDtos(usersWithoutTeamLead);
    }
}