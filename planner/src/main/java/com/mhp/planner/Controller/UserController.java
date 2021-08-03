package com.mhp.planner.Controller;

import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/getAll")
    public ResponseEntity<List<UserDto>> getAllUsers()
    {
        List<UserDto> userDtos = userService.getAllUsers();

        return ResponseEntity.ok(userDtos);
    }
}
