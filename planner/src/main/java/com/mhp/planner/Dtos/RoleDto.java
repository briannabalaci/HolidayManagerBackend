package com.mhp.planner.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class RoleDto {

    @JsonIgnore
    private Long id;

    private String name;
}
