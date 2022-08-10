package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {


    List<Holiday> findByUserId(Long userId);
}
