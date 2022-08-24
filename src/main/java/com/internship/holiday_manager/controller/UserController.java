package com.internship.holiday_manager.controller;

import com.internship.holiday_manager.dto.*;
import com.internship.holiday_manager.dto.user.UpdateUserDto;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.dto.user.UserNameDto;
import com.internship.holiday_manager.service.user_service.UserService;
import com.internship.holiday_manager.utils.annotations.AllowAdmin;
import com.internship.holiday_manager.utils.annotations.AllowAll;
import com.internship.holiday_manager.utils.annotations.AllowTeamLead;
import com.internship.holiday_manager.utils.annotations.AllowTeamLeadAndEmployee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/user")
@CrossOrigin()
@Slf4j
public class UserController {


    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/add-user")
    @AllowAdmin
    public ResponseEntity<String> addUser(@RequestBody RegisterDto dto) {
        if (!userService.userExists(dto)) {
            userService.createUser(dto);
            return new ResponseEntity<>("User created succesfully!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("The user already exists!", HttpStatus.OK);
        }
    }

    @PutMapping("/update-user")
    @AllowAdmin
    public ResponseEntity<UserDto> updateUser(@RequestBody UpdateUserDto dto) {
        return new ResponseEntity<>(userService.updateUser(dto), HttpStatus.OK);
    }

    @DeleteMapping("/delete-user/{email}")
    @AllowAdmin
    public ResponseEntity delete(@PathVariable String email) {
        userService.deleteUser(email);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/find-user-by-id/{id}")
    @AllowAll
    public ResponseEntity findById(@PathVariable Long id) {
        log.info("aici");
        return new ResponseEntity<>(userService.findUserById(id), HttpStatus.OK);
    }

    @GetMapping("/get-all-users")
    @AllowAdmin
    public ResponseEntity<List<UserDto>> getAll() {
        return new ResponseEntity<>(userService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/users-noteam")
    @AllowAdmin
    public ResponseEntity<List<UserDto>> getUsersWithoutTeam() {
        return new ResponseEntity<>(userService.getUsersWithoutTeam(), HttpStatus.OK);
    }

    @GetMapping("/user-info")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<UserDto> getUser(@RequestParam("email") String email) {
        return new ResponseEntity<>(this.userService.getUser(email), HttpStatus.OK);
    }

    @GetMapping("/user")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<UserDto> getUser(@RequestParam("id") Long id) {
        return new ResponseEntity<>(this.userService.getUserById(id), HttpStatus.OK);
    }

    @PutMapping("/update-vacation-days")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<UserDto> updateNoHolidaysUser(@RequestParam("email") String email, @RequestParam("noDays") Integer noDays) {
        return new ResponseEntity<>(this.userService.updateNoHolidaysUser(email, noDays), HttpStatus.OK);
    }

    @GetMapping("/filter-by-name")
    @AllowTeamLead
    public ResponseEntity<List<UserDto>> filterByName(@RequestBody UserNameDto userNameDto) {
        return new ResponseEntity<>(this.userService.filterByName(userNameDto), HttpStatus.OK);
    }

    @GetMapping("/substitutes")
    @AllowTeamLead
    public ResponseEntity<List<UserDto>> getAllUsersWithoutTeamLead(@RequestParam Long teamLeadId){
        return new ResponseEntity<>(this.userService.getAllUsersWithoutTeamLead(teamLeadId), HttpStatus.OK);
    }

}
