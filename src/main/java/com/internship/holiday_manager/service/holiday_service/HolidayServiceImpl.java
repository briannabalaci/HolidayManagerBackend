package com.internship.holiday_manager.service.holiday_service;

import com.internship.holiday_manager.dto.HolidayDto;
import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.Team;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.mapper.HolidayMapper;
import com.internship.holiday_manager.repository.HolidayRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class HolidayServiceImpl implements HolidayService{

    private final HolidayRepository holidayRepository;
    private final HolidayMapper holidayMapper;

    public HolidayServiceImpl(HolidayRepository holidayRepository, HolidayMapper holidayMapper) {
        this.holidayRepository = holidayRepository;
        this.holidayMapper = holidayMapper;
    }

    @Override
    public HolidayDto createHoliday(HolidayDto holidayDto) {
        Holiday entityToSave = holidayMapper.dtoToEntity(holidayDto);
        Holiday saved = holidayRepository.save(entityToSave);
        log.info("New holiday created");
        return holidayMapper.entityToDto(saved);
    }

    @Override
    public List<HolidayDto> getAll() {
        List<Holiday> entities = holidayRepository.findAll();
        return holidayMapper.entitiesToDtos(entities);
    }

    @Override
    public List<HolidayDto> getUsersHolidays(UserDto dto) {
        List<Holiday> entities = holidayRepository.findUsersHolidays(dto.getId());
        System.out.println(entities);
        return holidayMapper.entitiesToDtos(entities);
    }
}
