package com.mhp.planner.Controller;

import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    @GetMapping("/getAll")
    public ResponseEntity<List<UserDto>> getAllUsers()
    {
        List<UserDto> userDtos = userService.getAllUsers();

        return ResponseEntity.ok(userDtos);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDto userDto)
    {
        UserDto user = userService.findUser(userDto);

        if (user.getEmail() == null)
            return ResponseEntity.internalServerError().body("Incorrect email!");
        else
        {
            if (user.getPassword() == null)
                return ResponseEntity.internalServerError().body("Incorrect password!");
            else
                return ResponseEntity.ok(user);
        }
    }
}
