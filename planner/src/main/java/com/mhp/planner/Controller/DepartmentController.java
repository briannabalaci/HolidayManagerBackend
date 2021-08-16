package com.mhp.planner.Controller;

import com.mhp.planner.Dtos.DepartmentDto;
import com.mhp.planner.Services.DepartmentService;
import com.mhp.planner.Util.Annotations.AllowAdmin;
import com.mhp.planner.Util.Annotations.AllowAdminOrganizer;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/departments")
@AllArgsConstructor
@CrossOrigin
public class DepartmentController {
    private final DepartmentService departmentService;

    @AllowAdminOrganizer
    @GetMapping("/getAll")
    public ResponseEntity<List<DepartmentDto>> getAllDepartments()
    {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }
}
