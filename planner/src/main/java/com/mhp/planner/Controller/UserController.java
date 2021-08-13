package com.mhp.planner.Controller;

import com.mhp.planner.Config.JwtTokenService;
import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Dtos.UserPasswordDto;
import com.mhp.planner.Services.UserService;
import com.mhp.planner.Util.Annotations.AllowAdmin;
import com.mhp.planner.Util.Annotations.AllowAdminOrganizer;
import com.mhp.planner.Util.Annotations.AllowAll;
import com.mhp.planner.Util.Annotations.AllowOrganizer;
import com.mhp.planner.Util.Enums.EAppRoles;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final PasswordEncoder encoder;

    @GetMapping("/getAll")
    @AllowAdminOrganizer
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> userDtos = userService.getAllUsers();

        return ResponseEntity.ok(userDtos);
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> loginUser(@RequestBody UserDto userDto) {
//        UserDto user = userService.findUser(userDto);
//
//        if (user.getEmail() == null)
//            return ResponseEntity.internalServerError().body("Incorrect email!");
//        else {
//            if (user.getPassword() == null)
//                return ResponseEntity.internalServerError().body("Incorrect password!");
//            else
//                return ResponseEntity.ok(user);
//        }
//    }

    @AllowAdmin
    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        try {
            userService.createUser(userDto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>("User with this email already exists!", HttpStatus.BAD_REQUEST);
        }
    }

    @AllowAdmin
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @AllowAdmin
    @PutMapping("/update")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(userDto));
    }

    @AllowAll
    @PutMapping("/change-password")
    public ResponseEntity<HttpStatus> changePassword(@RequestBody UserPasswordDto userPasswordDto) {
        userService.changePasswordUser(userPasswordDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private final JwtTokenService jwtTokenService;

    @PostMapping(value="/login", produces="text/plain")
    @SneakyThrows
    public ResponseEntity<String> login(@RequestBody UserPasswordDto userPasswordDto) {

        UserDto user = userService.findUser(userPasswordDto);
        String jwt;

        if (user == null)
            return ResponseEntity.internalServerError().body("Incorrect email!");
        else {
            if (user.getPassword() == null)
                return ResponseEntity.internalServerError().body("Incorrect password!");
            else
                jwt = jwtTokenService.createJwtToken(user.getEmail(), Collections.singleton(EAppRoles.valueOf(user.getRole().toUpperCase())));
                    return ResponseEntity.ok(jwt);
        }
    }

    @GetMapping("/encrypt-stuff")
    public ResponseEntity encrypt(@RequestParam final String password){
        return ResponseEntity.ok(encoder.encode(password));
    }
}
