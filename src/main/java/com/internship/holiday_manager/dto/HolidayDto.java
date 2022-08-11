package com.internship.holiday_manager.dto;

import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HolidayDto {
    private Long id;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String substitute;

    private byte[] document;

    private HolidayType type;

    private HolidayStatus status;

    private String details;

    private UserWithTeamIdDto user;
}
