package com.mhp.planner.Services;

import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Dtos.UserPasswordDto;
import com.mhp.planner.Entities.User;
import com.mhp.planner.Mappers.UserMapper;
import com.mhp.planner.Repository.UserRepository;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    @Override
    public List<UserDto> getAllUsers() {
        List<User> userList = userRepository.findAll();

        System.out.println(userList);

        return userMapper.entities2dtos(userList);
    }

    @Override
    public UserDto findUser(UserPasswordDto userPasswordDto) {
        User user = userRepository.findByEmail(userPasswordDto.getEmail());
        UserDto u;
        if (user == null)
            return null;
        else {
            u = userMapper.entity2Dto(user);
            if (!encoder.matches(new String(Base64.getDecoder().decode(userPasswordDto.getPassword())), user.getPassword()))
                u.setPassword(null);
        }
        return u;
    }

    @Override
    public UserDto createUser(UserDto userDto) throws NotFoundException {
        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            throw new NotFoundException("User with this email already exists!");
        }

        User user = userMapper.dto2entity(userDto);
        user.setPassword(encoder.encode(new String(Base64.getDecoder().decode(user.getPassword()))));
        User createdUser = userRepository.save(user);
        return userMapper.entity2Dto(createdUser);
    }

    @Override
    public void deleteUser(Long id) throws NotFoundException {
        Optional<User> userOptional = userRepository.findById(id);
        if(userOptional.isEmpty()) {
            throw new NotFoundException("User with id " + id + " not found!");
        }
        else {
            userRepository.deleteById(id);
        }
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = userMapper.dto2entity(userDto);
        user.setPassword(encoder.encode(new String(Base64.getDecoder().decode(userDto.getPassword()))));
        User updatedUser = userRepository.save(user);
        return userMapper.entity2Dto(updatedUser);
    }

    @Override
    public UserDto changePasswordUser(UserPasswordDto userPasswordDto) {
        User user = userRepository.findByEmail(userPasswordDto.getEmail());

        user.setPassword(encoder.encode(new String(Base64.getDecoder().decode(userPasswordDto.getNewPassword()))));

        User updatedUser = userRepository.save(user);

        return userMapper.entity2Dto(updatedUser);
    }
}
