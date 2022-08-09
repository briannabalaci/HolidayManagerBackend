package com.internship.holiday_manager.controller;



import com.internship.holiday_manager.dto.*;

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
    public ResponseEntity<String> addUser(@RequestBody RegisterDto dto){
        if(!userService.userExists(dto)) {
            userService.createUser(dto);
            return new ResponseEntity<>("User created succesfully!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("The user already exists!", HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/update-user")
    @AllowAdmin
    public ResponseEntity<UserDto> updateUser(@RequestBody UpdateUserDto dto){
        return new ResponseEntity<>(userService.updateUser(dto), HttpStatus.OK);
    }

    @DeleteMapping("/delete-user/{email}")
    @AllowAdmin
    public ResponseEntity delete(@PathVariable String email) {
        userService.deleteUser(email);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/get-all-users")
    @AllowAdmin
    public ResponseEntity<List<UserDto>> getAll(){
        return new ResponseEntity<>(userService.getAll(),HttpStatus.OK);
    }

    @GetMapping("/filterByName/{forname}/{surname}")
    @AllowAdmin
    public ResponseEntity<List<UserDto>> filterUsersByFornameAndSurname(@PathVariable String forname, @PathVariable String surname){
        return new ResponseEntity<>(userService.findAllByFornameOrSurname(forname,surname),HttpStatus.OK);
    }

}
