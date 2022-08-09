package com.internship.holiday_manager.service.teamlead_service;

import com.internship.holiday_manager.dto.HolidayDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.mapper.HolidayMapper;
import com.internship.holiday_manager.repository.HolidayRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamLeadServiceImpl implements TeamLeadService{

    private final HolidayRepository holidayRepository;
    private final HolidayMapper holidayMapper;

    public TeamLeadServiceImpl(HolidayRepository holidayRepository, HolidayMapper holidayMapper) {
        this.holidayRepository = holidayRepository;
        this.holidayMapper = holidayMapper;
    }

    @Override
    public List<HolidayDto> getRequests() {
        List<Holiday> entities = this.holidayRepository.findAll();
        return holidayMapper.entitiesToDtos(entities);

    }
}
