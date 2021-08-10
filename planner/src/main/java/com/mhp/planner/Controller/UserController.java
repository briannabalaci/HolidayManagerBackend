package com.mhp.planner.Controller;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Services.UserService;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @PostMapping("/create-user")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
