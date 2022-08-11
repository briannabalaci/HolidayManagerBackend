package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.enums.HolidayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findAllByTypeAndUserId(HolidayType type, Long user_id);
    List<Holiday> findByUserId(Long userId);
}
