package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.HolidayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday,Long> {

    @Query("select h from Holiday h where h.user.id=:id ")
    List<Holiday> findUsersHolidays(@Param("id") Long id);

    @Query("select u from Holiday u where u.id=:id")
    Holiday findByID(@Param("id") Long id);

    List<Holiday> findAllByTypeAndUserId(HolidayType type, Long user_id);

    List<Holiday> findByUserId(Long userId);
}

