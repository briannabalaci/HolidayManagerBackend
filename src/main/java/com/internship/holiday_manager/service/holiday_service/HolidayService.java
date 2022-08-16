package com.internship.holiday_manager.service.holiday_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;

import java.util.List;

public interface HolidayService {

    /**
     * If the user which created the holiday is an Employee, the status will be Pending.
     * If the user which created the holiday is a TeamLead, the status will be Approved.
     * @param holidayDto -> the request for which we will set the status
     * @return -> the updated request
     */
    HolidayDto setStatusHoliday(HolidayDto holidayDto);

    HolidayDto createHoliday(HolidayDto holidayDto);

    HolidayDto updateHoliday(HolidayDto holidayDto);

    List<HolidayDto> getAll();

    List<HolidayDto> getUsersHolidays(Long id);

    HolidayDto deleteHoliday(Long id);
}
