package com.mhp.planner.Services;

import com.mhp.planner.Dtos.RoleDto;
import com.mhp.planner.Entities.Role;
import com.mhp.planner.Mappers.RoleMapper;
import com.mhp.planner.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService{
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.entities2dtos(roles);
    }
}
