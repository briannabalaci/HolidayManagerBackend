package com.internship.holiday_manager.service.holiday_service;

import com.internship.holiday_manager.dto.HolidayDto;
import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.UserType;
import com.internship.holiday_manager.mapper.HolidayMapper;
import com.internship.holiday_manager.repository.HolidayRepository;
import com.internship.holiday_manager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class HolidayServiceImpl implements HolidayService{

    private final UserRepository userRepository;
    private final HolidayRepository holidayRepository;
    private final HolidayMapper holidayMapper;

    public HolidayServiceImpl(UserRepository userRepository, HolidayRepository holidayRepository, HolidayMapper holidayMapper) {
        this.userRepository = userRepository;
        this.holidayRepository = holidayRepository;
        this.holidayMapper = holidayMapper;
    }

    @Override
    public HolidayDto createHoliday(HolidayDto holidayDto) {
        HolidayDto updatedHolidayDto = this.setStatusHoliday(holidayDto);

        Holiday entityToSave = holidayMapper.dtoToEntity(updatedHolidayDto);

        Holiday saved = holidayRepository.save(entityToSave);

        log.info("New holiday created");
        return holidayMapper.entityToDto(saved);
    }
    private void ChangeHolidayData(HolidayDto dto, Holiday u){
        u.setStatus(dto.getStatus());
        u.setEndDate(dto.getEndDate());
        u.setStartDate(dto.getStartDate());
        u.setSubstitute(dto.getSubstitute());
    }

    //TO DO - not done!!
    @Override
    public HolidayDto updateHoliday(HolidayDto holidayDto) {
        Holiday u = holidayRepository.findByID(holidayDto.getId());
        if(u!= null) {
            ChangeHolidayData(holidayDto,u);
        }
        return holidayMapper.entityToDto(holidayRepository.save(u));
    }

    @Override
    public List<HolidayDto> getAll() {
        List<Holiday> entities = holidayRepository.findAll();
        return holidayMapper.entitiesToDtos(entities);
    }

    @Override
    public List<HolidayDto> getUsersHolidays(Long id) {
        List<Holiday> entities = holidayRepository.findUsersHolidays(id);
        return holidayMapper.entitiesToDtos(entities);
    }

    @Override
    public HolidayDto setStatusHoliday(HolidayDto holidayDto) {
        Long userId = holidayDto.getUser().getId();
        if(this.userRepository.getById(userId).getType().equals(UserType.TEAMLEAD)){
            holidayDto.setStatus(HolidayStatus.APPROVED);
        }
        else{
            holidayDto.setStatus(HolidayStatus.PENDING);
        }
        return holidayDto;
    }
}
