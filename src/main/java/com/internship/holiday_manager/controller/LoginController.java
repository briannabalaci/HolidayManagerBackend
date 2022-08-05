package com.internship.holiday_manager.controller;

import com.internship.holiday_manager.config.JwtTokenService;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.service.user_service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import static java.lang.StringCoding.encoder;


@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;
    private  final JwtTokenService jwtTokenService;

    public LoginController(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/auth")
    public ResponseEntity<String> authentication(@RequestBody LoginUserDto dto){

        User userInfo = userService.getUserInformation(dto);
        String jwt = jwtTokenService.createJwtToken(userInfo.getEmail(), userInfo.getType());
        return ResponseEntity.ok(jwt);
    }

    //TODO: change-password method
//    @PutMapping("/change-password")
//    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDto dto){
//        if(userService.verifyPasswordAndCredentials(dto)){
//            userService.changePassword(dto);
//            return new ResponseEntity<>("Password was changed successfully!", HttpStatus.OK);
//        }
//        return new ResponseEntity<>("Invalid password or email", HttpStatus.OK);
//    }

}
