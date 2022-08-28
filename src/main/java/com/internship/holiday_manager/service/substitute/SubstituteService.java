package com.internship.holiday_manager.service.substitute;

import com.internship.holiday_manager.dto.substitute.SubstituteDto;
import com.internship.holiday_manager.dto.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

public interface SubstituteService {

    List<SubstituteDto> getAll();
    List<UserDto> teamLeadersForWhichSubstituteIsActive(Long substituteId);
}
