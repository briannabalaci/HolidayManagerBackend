package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday,Long> {
}
