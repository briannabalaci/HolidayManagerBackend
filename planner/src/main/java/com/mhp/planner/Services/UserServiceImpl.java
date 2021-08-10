package com.mhp.planner.Services;

import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Entities.User;
import com.mhp.planner.Mappers.UserMapper;
import com.mhp.planner.Repository.UserRepository;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

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

    @Override
    public UserDto findUser(UserDto userDto) {
        Optional<User> user = userRepository.findByEmail(userDto.getEmail());

        if (user.isEmpty())
            userDto.setEmail(null);
        else {
            User u = user.get();
            if (!u.getPassword().equals(userDto.getPassword()))
                userDto.setPassword(null);
            else
                userDto = userMapper.entity2Dto(u);
        }

        return userDto;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists!");
        }

        User user = userMapper.dto2entity(userDto);
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
}
