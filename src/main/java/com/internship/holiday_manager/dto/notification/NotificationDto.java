package com.internship.holiday_manager.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.user.UserWithTeamIdDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.NotificationType;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
@Data
public class NotificationDto {
    private Long id;

    private UserWithTeamIdDto sender;

    private UserWithTeamIdDto receiver;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendDate;

    private Boolean seen;

    private NotificationType type;

    private HolidayDto request;
}
