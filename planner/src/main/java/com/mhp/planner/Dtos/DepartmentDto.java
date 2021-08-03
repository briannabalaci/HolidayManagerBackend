package com.mhp.planner.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class DepartmentDto {

    @JsonIgnore
    private Long id;

    private String name;
}
