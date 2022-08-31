package com.internship.holiday_manager.service.substitute;

import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.substitute.SubstituteDto;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.dto.user.UserWithTeamIdDto;

import java.time.LocalDateTime;
import java.util.List;

public interface SubstituteService {

    List<SubstituteDto> getAll();
    List<UserDto> teamLeadersForWhichSubstituteIsActive(Long substituteId);

    public UserDto getSubstituteOfTeamLead(Long holidayId);
}
