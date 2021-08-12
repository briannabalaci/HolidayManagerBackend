package com.mhp.planner.Controller;

import com.mhp.planner.Util.Annotations.AllowAll;
import com.mhp.planner.Util.Annotations.AllowAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class SecurityTestController {

    private final PasswordEncoder encoder;

    @GetMapping("/access")
    @AllowAll
    public ResponseEntity access(){
        return ResponseEntity.ok().build();
    }

    @GetMapping("/no-access")
    @AllowAdmin
    public ResponseEntity noAccess(){
        return ResponseEntity.ok().build();
    }

    @GetMapping("/free-access")
    public ResponseEntity freeAccess(){
        return ResponseEntity.ok().build();
    }

    @GetMapping("/encrypt-stuff")
    public ResponseEntity encrypt(@RequestParam final String password){
        return ResponseEntity.ok(encoder.encode(password));
    }

    @GetMapping("/test-encrypt")
    public ResponseEntity testEncrypt(@RequestParam final String password){
        return ResponseEntity.ok(encoder.matches("test",password));
    }
}

