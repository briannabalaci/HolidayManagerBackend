package com.internship.holiday_manager.service.teamlead_service;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.entity.enums.HolidayType;

import java.util.List;

public interface TeamLeadService {

    /**
     *  We get all the holiday requests of the team-lead
     * @param teamLeaderId - the id of the team-lead
     * @return - a list with the requests
     */
    List<HolidayDto> getRequests(Long teamLeaderId);


    /**
     * We get all the holiday requests of the members which are part of the team of the team-lead
     * @param teamId - the id of the team-lead
     * @return - a list with the requests
     */
    List<HolidayDto> getTeamRequests(Long teamId);


}
