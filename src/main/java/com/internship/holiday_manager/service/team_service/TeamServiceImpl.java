package com.internship.holiday_manager.service.team_service;

import com.internship.holiday_manager.dto.TeamAddDto;
import com.internship.holiday_manager.dto.TeamDto;
import com.internship.holiday_manager.entity.Team;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.UserType;
import com.internship.holiday_manager.mapper.TeamMapper;
import com.internship.holiday_manager.repository.TeamRepository;
import com.internship.holiday_manager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TeamServiceImpl implements TeamService{
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;

    public TeamServiceImpl(TeamRepository teamRepository, UserRepository userRepository, TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.teamMapper = teamMapper;
    }


    @Override
    public TeamDto save(TeamAddDto teamDTO) {
        User teamLead = userRepository.getById(teamDTO.getTeamLeaderId());
        teamLead.setType(UserType.TEAMLEAD);
        userRepository.save(teamLead);
        
        Team entityToSave = Team.builder().name(teamDTO.getName())
                .teamLeader(teamLead.getForname()+ " "+teamLead.getSurname())
                .build();
        Team saved = teamRepository.save(entityToSave);
        log.info("New team created");

        for(Long userID:teamDTO.getMembersId()){
            User user = userRepository.getById(userID);
            user.setTeam(saved);
            userRepository.save(user);
        }
        log.info("TeamID updated for users.");
        return teamMapper.entityToDto(saved);
    }

    @Override
    public TeamDto findByID(Long teamID) {
        Team entity = teamRepository.getById(teamID);
        log.info("Get team by ID called. ");
        return teamMapper.entityToDto(entity);
    }

    @Override
    public TeamDto delete(Long teamID) {
        Team entity = teamRepository.getById(teamID);
        teamRepository.delete(entity);
        log.info("Team with ID {} deleted. "+teamID);
        return teamMapper.entityToDto(entity);
    }

    @Override
    public List<TeamDto> getAllTeams() {
        List<Team> entities = teamRepository.findAll();
        log.info("Get all teams called.");
        List<TeamDto> dtos = teamMapper.entitiesToDtos(entities);
        return dtos;

    }


}
