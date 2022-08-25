package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.Notification;
import com.internship.holiday_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    @Query("select u from Notification u where u.id=:id")
    Notification findByID(@Param("id") Long id);

    @Query("select n from Notification n where n.receiver.id=:id and n.seen=:seen")
    List<Notification> getAllUsersNotif(@Param("id") Long id, @Param("seen") Boolean seen);
    @Modifying
    @Transactional
    @Query("update Notification n set n.seen=true where n.receiver.id=:id")
    void seenAll(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("delete from Notification n where n.request.id=:id ")
    void deleteHolidaysNotification(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("delete from Notification n where n.receiver.id=:id and n.seen=true")
    void deleteSeenNotifications(@Param("id") Long id);

    List<Notification> findByReceiver(User user);

    List<Notification> findBySender(User user);

}
