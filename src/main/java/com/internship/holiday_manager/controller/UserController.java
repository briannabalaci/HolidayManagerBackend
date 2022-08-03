package com.internship.holiday_manager.controller;


import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

<<<<<<< HEAD
    @PostMapping("/login-user")
    public ResponseEntity<String> authentication(@RequestBody UserDto dto){
=======
    @PostMapping("/login")
    public ResponseEntity<User> authentication(@RequestBody LoginUserDto dto){
>>>>>>> d30611a10039645b6b9c9d688da1a9babeacd77c
        return new ResponseEntity<>(userService.authentication(dto.getEmail(), dto.getPassword()), HttpStatus.OK);
    }
}
