package com.mhp.planner.Controller;

import com.mhp.planner.Dtos.RoleDto;
import com.mhp.planner.Services.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@AllArgsConstructor
@CrossOrigin
public class RoleController {
    private final RoleService roleService;

    @GetMapping("/getAll")
    public ResponseEntity<List<RoleDto>> getAllRoles()
    {
        return ResponseEntity.ok(roleService.getAllRoles());
    }
}
