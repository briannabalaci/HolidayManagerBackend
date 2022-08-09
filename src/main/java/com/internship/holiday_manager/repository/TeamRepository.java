package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {
}
