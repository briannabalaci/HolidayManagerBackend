package com.internship.holiday_manager.controller;

import com.internship.holiday_manager.config.JwtTokenService;
import com.internship.holiday_manager.dto.ChangePasswordDto;
import com.internship.holiday_manager.dto.LoginUserDto;
import com.internship.holiday_manager.dto.TokenDto;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.service.user_service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/login")
@CrossOrigin()
public class LoginController {


    private final UserService userService;
    private  final JwtTokenService jwtTokenService;

    public LoginController(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/auth")
    public ResponseEntity<TokenDto> authentication(@RequestBody LoginUserDto dto){

        User userInfo = userService.getUserInformation(dto);
        String jwt = "";
        if(userInfo != null) {
            jwt = jwtTokenService.createJwtToken(userInfo.getEmail(), userInfo.getType());
        }
        TokenDto token = new TokenDto();
        token.setToken(jwt);
        return ResponseEntity.ok(token);
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDto dto){
        if(userService.verifyPasswordAndCredentials(dto)){
            userService.changePassword(dto);
            return new ResponseEntity<>("Password was changed successfully!", HttpStatus.OK);
        }
        return new ResponseEntity<>("Invalid password or email", HttpStatus.OK);
    }

}
