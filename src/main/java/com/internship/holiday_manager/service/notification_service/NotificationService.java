package com.internship.holiday_manager.service.notification_service;

import com.internship.holiday_manager.dto.RegisterDto;
import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.notification.NotificationDto;
import com.internship.holiday_manager.dto.user.UserDto;

import java.util.List;

public interface NotificationService {

    NotificationDto createNotification(NotificationDto dto);

    List<NotificationDto> getAll();

    NotificationDto deleteNotification(Long id);
}
