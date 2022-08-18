package com.internship.holiday_manager.service.notification_service;

import com.internship.holiday_manager.dto.RegisterDto;
import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.notification.NotificationDto;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.entity.Notification;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationService {

    /**
     * create new notification
     * @param dto of type NotificationDto is he notification that is to be saved
     * @return the saved notification
     */
    NotificationDto createNotification(NotificationDto dto);

    /**
     * @return the list of all notifications
     */
    List<NotificationDto> getAll();

    /**
     * @param id the id of the notification we want to delete
     * @return the notification we deleted in case of success
     */
    NotificationDto deleteNotification(Long id);

    /**
     * @param id id ul user ului care a primit notificarea
     * @param seen selecteaza notificarile vazute sau nevazute
     * @return returneaza notificarile vazute sau nevazute ale unui user
     */
    List<NotificationDto> getAllUsersNotif(Long id, Boolean seen);

    /**
     * Updateaza toate notificarile unui user cu id ul id sa fie vazute
     * @param id id ul user ului care a vazut notificarile
     */
    void setSeen(Long id);

}
