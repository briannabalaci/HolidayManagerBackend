package com.internship.holiday_manager.service.holiday_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.holiday.UpdateDetailsHolidayDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.enums.HolidayStatus;

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

    /**
     * Approve a holiday request
     * @param id = the id of the holiday request that has to be approved
     * @return the approved holiday request
     */
    HolidayDto approveHolidayRequest(Long id);
    /**
     * Deny a holiday request
     * @param id = the id of the holiday request that has to be denied
     * @return the denied holiday request
     */
    HolidayDto denyHolidayRequest(Long id);

    /**
     * Update <<details>> field if the teamlead needs more details
     * @param updateDetailsHolidayDto = contains the id the holiday request that the teamlead needs more details about + the string with the details
     * @return the updated holiday
     */
    HolidayDto requestMoreDetails(UpdateDetailsHolidayDto updateDetailsHolidayDto);
}
