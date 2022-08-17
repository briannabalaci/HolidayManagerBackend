package com.internship.holiday_manager.service.holiday_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;

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


    /**
     * We get all the holiday requests with the given type
     * @param type - the type by which we filter the holidays
     * @return - a list with the filtered holidays
     */
    List<HolidayDto> getRequestsByType(Long teamLeaderId, HolidayType type);

    /**
     * We get all the holiday requests with the given status
     * @param status - the status by which we filter the holidays
     * @return - a list with the filtered holidays
     */
    List<HolidayDto> getRequestsByStatus(Long teamLeaderId, HolidayStatus status);

    /**
     * We get all the holiday requests with the given status and type
     * @param type - the type by which we filter the holidays
     * @param status - the status by which we filter the holidays
     * @return - a list with the filtered holidays
     */
    List<HolidayDto> getRequestsByStatusAndType(Long teamLeaderId, HolidayStatus status, HolidayType type);

}
