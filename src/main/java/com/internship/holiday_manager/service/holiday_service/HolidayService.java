package com.internship.holiday_manager.service.holiday_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.holiday.HolidayTypeAndUserName;
import com.internship.holiday_manager.dto.holiday.UpdateDetailsHolidayDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;

import java.time.LocalDateTime;
import java.util.List;

public interface HolidayService {

    /**
     * If the user which created the holiday is an Employee, the status will be Pending.
     * If the user which created the holiday is a TeamLead, the status will be Approved.
     * @param holidayDto -> the request for which we will set the status
     * @return -> the updated request
     */
    HolidayDto setStatusHoliday(HolidayDto holidayDto);


    HolidayDto updateHolidayRequest(HolidayDto holidayDto);

    HolidayDto createHoliday(HolidayDto holidayDto);

    HolidayDto updateHoliday(HolidayDto holidayDto);

    List<HolidayDto> getAll();

    List<HolidayDto> getUsersHolidays(Long id);

    HolidayDto deleteHoliday(Long id);

    /**
     * Computes the number of unpaid vacations days for the given period
     * @param days - the number of days of the vacation (the period of the vacation)
     * @return - the number of unpaid vacations days
     */
    Integer getNoUnpaidDays(Integer days);

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


    /**
     * We compute the number of days from the given interval
     * @param start - start date of the interval
     * @param end - end date of the interval
     * @return - the number of days from the interval
     */
    Integer getNoHolidays(LocalDateTime start, LocalDateTime end);


    /**
     * Searches the holiday based on its id
     * @param id - the id of the holiday
     * @return - the found holiday
     */
    HolidayDto getHolidayById(Long id);

    List<HolidayDto> filterByTypeAndUserName(HolidayTypeAndUserName dto);
    List<HolidayDto> filterByType(Long teamLeaderId, HolidayType type);
    List<HolidayDto> filterByUserName(Long teamLeaderId, String forname, String surname);

    /**
     * Check if the user has the necessary days in order to create the request
     * @param email - the email of the user
     * @param type - the type of the holiday
     * @param startDate - the start date of the holiday
     * @param endDate - the end date of the holiday
     * @return - returns 1 if the request can be created and 0 otherwise
     */
    Integer checkRequestCreate(String email, HolidayType type,  String startDate, String endDate);


    /**
     * Check if the user has the necessary days in order to update the request's date
     * @param email - the email of the user
     * @param type - the type of the holiday
     * @param startDate - the start date of the holiday
     * @param endDate - the end date of the holiday
     * @param holidayId - the id of the holiday before update
     * @return - returns 1 if the request can be created and 0 otherwise
     */
    Integer checkRequestUpdate(String email, HolidayType type,  String startDate, String endDate, Long holidayId);

    Integer checkIfDatesOverlap(String email, String startDate, String endDate);

    Integer checkIfDatesOverlapUpdate(String email, String startDate, String endDate, Long holidayId);
}
