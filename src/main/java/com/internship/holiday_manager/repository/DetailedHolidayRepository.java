package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.entity.DetailedHoliday;
import com.internship.holiday_manager.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetailedHolidayRepository extends JpaRepository<DetailedHoliday,Long> {

    DetailedHoliday findByHoliday(Holiday h);
}

