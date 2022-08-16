package com.internship.holiday_manager.controller;


import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.notification.NotificationDto;
import com.internship.holiday_manager.service.notification_service.NotificationService;
import com.internship.holiday_manager.utils.annotations.AllowAll;
import com.internship.holiday_manager.utils.annotations.AllowEmployee;
import com.internship.holiday_manager.utils.annotations.AllowTeamLeadAndEmployee;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@CrossOrigin
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/add-notification")
    @AllowAll
    public ResponseEntity<NotificationDto> addNotification(@RequestBody NotificationDto dto){
        return new ResponseEntity<>(notificationService.createNotification(dto), HttpStatus.OK);
    }

    @GetMapping("/get-all-notifications")
    @AllowAll
    public ResponseEntity<List<NotificationDto>> getAll(){
        return new ResponseEntity<>(notificationService.getAll(),HttpStatus.OK);
    }

    @DeleteMapping("/delete-notification/{id}")
    @AllowAll
    public ResponseEntity<String> deleteNotification(@PathVariable Long id) {
        if (notificationService.deleteNotification(id) != null) {
            return new ResponseEntity("Stergere finalizata cu succes", HttpStatus.OK);
        } else
            return new ResponseEntity("Stergere esuata", HttpStatus.CONFLICT);
    }
}
