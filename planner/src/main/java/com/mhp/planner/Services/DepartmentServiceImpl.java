package com.mhp.planner.Services;

import com.mhp.planner.Dtos.DepartmentDto;
import com.mhp.planner.Entities.Department;
import com.mhp.planner.Mappers.DepartmentMapper;
import com.mhp.planner.Repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService{
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public List<DepartmentDto> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return departmentMapper.entities2dtos(departments);
    }
}
