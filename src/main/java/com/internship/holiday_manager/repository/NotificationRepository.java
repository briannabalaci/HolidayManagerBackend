package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    @Query("select u from Notification u where u.id=:id")
    Notification findByID(@Param("id") Long id);
}
