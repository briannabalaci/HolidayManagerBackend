package com.internship.holiday_manager.controller;


import com.internship.holiday_manager.dto.UserDto;
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

    @PostMapping("/login-user")
    public ResponseEntity<String> authentication(@RequestBody UserDto dto){
        return new ResponseEntity<>(userService.authentication(dto.getEmail(), dto.getPassword()), HttpStatus.OK);
    }
}
