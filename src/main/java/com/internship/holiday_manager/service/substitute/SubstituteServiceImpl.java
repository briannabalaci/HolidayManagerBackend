package com.internship.holiday_manager.service.substitute;

import com.internship.holiday_manager.dto.substitute.SubstituteDto;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.dto.user.UserWithTeamIdDto;
import com.internship.holiday_manager.entity.Substitute;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.mapper.SubstituteMapper;
import com.internship.holiday_manager.mapper.UserMapper;
import com.internship.holiday_manager.mapper.UserWithTeamIdMapper;
import com.internship.holiday_manager.repository.SubstituteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

@Service
@Slf4j
public class SubstituteServiceImpl implements SubstituteService {

    private final SubstituteRepository substituteRepository;
    private final SubstituteMapper substituteMapper;
    private final UserWithTeamIdMapper userWithTeamIdMapper;
    private final UserMapper userMapper;

    public SubstituteServiceImpl(SubstituteRepository substituteRepository, SubstituteMapper substituteMapper, UserWithTeamIdMapper userWithTeamIdMapper, UserMapper userMapper) {
        this.substituteRepository = substituteRepository;
        this.substituteMapper = substituteMapper;
        this.userWithTeamIdMapper = userWithTeamIdMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<SubstituteDto> getAll() {
        List<Substitute> entities = substituteRepository.findAll();
        return substituteMapper.entitiesToDtos(entities);
    }

    @Override
    public List<UserDto> teamLeadersForWhichSubstituteIsActive(Long substituteId) {
        LocalDateTime now = LocalDateTime.now();
        List<User> teamLeaders = substituteRepository.findAll()
                .stream()
                .filter(s -> (s.getStartDate().isBefore(now()) || s.getStartDate().isEqual(now())) && (now().isBefore(s.getEndDate()) || now().isEqual(s.getEndDate())))
                .filter( s -> s.getSubstitute().getId().equals(substituteId))
                .map( s -> s.getTeamLead())
                .collect(Collectors.toList());
        return userMapper.entitiesToDtos(teamLeaders);
    }

    public UserDto getSubstituteOfTeamLead(UserWithTeamIdDto userDto) {
        User user = userWithTeamIdMapper.dtoToEntity(userDto);
        User rez = substituteRepository.findByTeamLead(user).get(0).getSubstitute();
        return userMapper.entityToDto(rez);
    }

}
