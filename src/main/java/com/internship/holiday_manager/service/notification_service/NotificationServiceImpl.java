package com.internship.holiday_manager.service.notification_service;

import com.internship.holiday_manager.dto.notification.NotificationDto;
import com.internship.holiday_manager.entity.Notification;
import com.internship.holiday_manager.mapper.NotificationMapper;
import com.internship.holiday_manager.repository.NotificationRepository;
import com.internship.holiday_manager.service.websocket_service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final WebSocketService webSocketService;


    public NotificationServiceImpl(NotificationRepository notificationRepository, NotificationMapper notificationMapper, WebSocketService webSocketService) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.webSocketService = webSocketService;
    }


    private void notifyFrontend(String topic, String message){
        webSocketService.sendMessage(topic, message);
    }
    @Override
    public NotificationDto createNotification(NotificationDto dto) {
        Notification entityToSave = notificationMapper.dtoToEntity(dto);
        Notification saved = notificationRepository.save(entityToSave);
        log.info("New notification created");
        notifyFrontend("notification","New notification! ");

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

    @Override
    public List<NotificationDto> getAllUsersNotif(Long id, Boolean seen) {
        List<Notification> entities = notificationRepository.getAllUsersNotif(id,seen);
        Collections.sort(entities, new Comparator<Notification>(){
            @Override
            public int compare(Notification n1, Notification n2){
                return n2.getSendDate().compareTo(n1.getSendDate());
            }
        });
        return notificationMapper.entitiesToDtos(entities);
    }

    @Override
    public void setSeen(Long id) {
        notificationRepository.seenAll(id);
    }
}
