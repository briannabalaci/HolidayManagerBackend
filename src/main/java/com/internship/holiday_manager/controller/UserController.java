package com.internship.holiday_manager.controller;


import com.internship.holiday_manager.dto.ChangePasswordDto;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.service.user_service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@CrossOrigin()
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<User> authentication(@RequestBody LoginUserDto dto){
        return new ResponseEntity<>(userService.authentication(dto), HttpStatus.OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDto dto){
        if(userService.verifyPasswordAndCredentials(dto)){
            userService.changePassword(dto);
            return new ResponseEntity<>("Password was changed successfully!", HttpStatus.OK);
        }
            return new ResponseEntity<>("Invalid password or email", HttpStatus.OK);
    }

    @PostMapping("/add-user")
    public ResponseEntity<UserDto> addUser(@RequestBody UserDto dto){
        return new ResponseEntity<>(userService.createUser(dto), HttpStatus.OK);
    }
    
}
