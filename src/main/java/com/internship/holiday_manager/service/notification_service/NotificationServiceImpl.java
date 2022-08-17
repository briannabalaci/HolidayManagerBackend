package com.internship.holiday_manager.service.notification_service;

import com.internship.holiday_manager.dto.RegisterDto;
import com.internship.holiday_manager.dto.notification.NotificationDto;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.Notification;
import com.internship.holiday_manager.mapper.NotificationMapper;
import com.internship.holiday_manager.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public NotificationDto createNotification(NotificationDto dto) {
        Notification entityToSave = notificationMapper.dtoToEntity(dto);
        Notification saved = notificationRepository.save(entityToSave);
        log.info("New notification created");
        return notificationMapper.entityToDto(saved);
    }

    @Override
    public List<NotificationDto> getAll() {
        List<Notification> entities = notificationRepository.findAll();
        return notificationMapper.entitiesToDtos(entities);
    }

    @Override
    public NotificationDto deleteNotification(Long id) {
        Notification notification = notificationRepository.findByID(id);
        if (notification != null) {
            notificationRepository.delete(notification);
            return notificationMapper.entityToDto(notification);
        }
        return null;
    }
}
