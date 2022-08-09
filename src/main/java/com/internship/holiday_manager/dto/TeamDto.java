package com.internship.holiday_manager.dto;

import com.internship.holiday_manager.entity.User;
import lombok.Data;

import javax.persistence.Column;
import java.util.List;

@Data
public class TeamDto {
    private Long id;
    private String name;
    private String teamLeader;
    private List<User> members;
}
