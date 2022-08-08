package com.internship.holiday_manager.controller;



import com.internship.holiday_manager.dto.ChangePasswordDto;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.dto.UpdateUserDto;

import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.service.user_service.UserService;
import com.internship.holiday_manager.utils.annotations.AllowAdmin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/user")
@CrossOrigin()
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }




    @PostMapping("/add-user")
    @AllowAdmin
    public ResponseEntity<UserDto> addUser(@RequestBody UserDto dto){
        return new ResponseEntity<>(userService.createUser(dto), HttpStatus.OK);
    }

    @PutMapping("/update-user")
    public ResponseEntity<UserDto> updateUser(@RequestBody UpdateUserDto dto){
        return new ResponseEntity<>(userService.updateUser(dto), HttpStatus.OK);
    }

    @DeleteMapping("/delete-user/{email}")
    public ResponseEntity delete(@PathVariable String email) {
        userService.deleteUser(email);
        return new ResponseEntity(HttpStatus.OK);
    }
    @GetMapping("/get-all-users")
    public ResponseEntity<List<UserDto>> getAll(){
        return new ResponseEntity<>(userService.getAll(),HttpStatus.OK);
    }

}
