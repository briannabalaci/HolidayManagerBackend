package com.internship.holiday_manager.service.teamlead_service;

import com.internship.holiday_manager.dto.HolidayDto;

import java.util.List;

public interface TeamLeadService {

    /**
     *  We get all the holiday requests of the team-lead
     * @param teamLeaderId - the d of the team-lead
     * @return - a list with the requests
     */
    List<HolidayDto> getRequests(Long teamLeaderId);

}
