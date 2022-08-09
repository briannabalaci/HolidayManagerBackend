package com.internship.holiday_manager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HolidayDto {
    private Long id;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start_date;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end_date;
    private String substitute;
    private byte[] document;
    private HolidayType type;
    private HolidayStatus status;
    private String details;
}
