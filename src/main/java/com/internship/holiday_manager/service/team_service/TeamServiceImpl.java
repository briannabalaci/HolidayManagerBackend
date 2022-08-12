package com.internship.holiday_manager.service.team_service;

import com.internship.holiday_manager.dto.team.TeamAddDto;
import com.internship.holiday_manager.dto.team.TeamDto;
import com.internship.holiday_manager.dto.team.TeamUpdateDto;
import com.internship.holiday_manager.entity.Team;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.UserType;
import com.internship.holiday_manager.mapper.TeamMapper;
import com.internship.holiday_manager.mapper.UserMapper;
import com.internship.holiday_manager.repository.TeamRepository;
import com.internship.holiday_manager.repository.UserRepository;
import liquibase.pro.packaged.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TeamServiceImpl implements TeamService{
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;
    private final UserMapper userMapper;

    public TeamServiceImpl(TeamRepository teamRepository, UserRepository userRepository, TeamMapper teamMapper, UserMapper userMapper) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.teamMapper = teamMapper;
        this.userMapper = userMapper;
    }

    private void revertTeamLeadToEmployee(Team team){
        User teamLead = team.getTeamLeader();
        teamLead.setType(UserType.EMPLOYEE);
        userRepository.save(teamLead);
    }

    private Team createTeam(TeamAddDto teamDTO) throws Exception {
        if(teamRepository.findByName(teamDTO.getName()) != null) throw new Exception("A team with the same name already exists!\n");

        User teamLead = userRepository.getById(teamDTO.getTeamLeaderId());
        Team entityToSave = Team.builder().name(teamDTO.getName())
                .teamLeader(teamLead)
                .build();
        log.info(entityToSave.toString());
        Team saved = teamRepository.save(entityToSave);
        log.info("SAVED: "+saved.toString());
        log.info("New team created");
        return saved;
    }
    private void updateUsersTeam(User teamLead, List<Long> members, Team savedTeam){
        for(Long userID: members){
            User user = userRepository.getById(userID);
            user.setTeam(savedTeam);
            if(user.getId().equals(teamLead.getId())){
                teamLead.setType(UserType.TEAMLEAD);
            }
            userRepository.save(user);
        }
        log.info("TeamID updated for users.");
    }

    @Override
    public TeamDto save(TeamAddDto teamDTO) throws Exception {
        Team team;
        try {
            team = createTeam(teamDTO);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        updateUsersTeam(team.getTeamLeader(),teamDTO.getMembersId(),team);

        return teamMapper.entityToDto(team);
    }

    private Team updateTeam(TeamUpdateDto teamDTO){
        Team team = teamRepository.getById(teamDTO.getId());
        User oldTeamLead = team.getTeamLeader();
        if(!oldTeamLead.getId().equals(teamDTO.getTeamLeaderId())) {
            revertTeamLeadToEmployee(team);
            team.setTeamLeader(userRepository.getById(teamDTO.getTeamLeaderId()));
        }
        team.setName(teamDTO.getName());
        team.getMembers().clear();
        List<User> members = new ArrayList<>();
        for(Long userID:teamDTO.getMembersId()) {
            User x = userRepository.getById(userID);
            x.setTeam(null);
            userRepository.save(x);
            members.add(x);
        }
        team.setMembers(members);

        Team saved = teamRepository.save(team);
        log.info("Team updated");
        return saved;
    }
    @Override
    public TeamDto update(TeamUpdateDto teamDTO) {
        Team team = updateTeam(teamDTO);
        updateUsersTeam(team.getTeamLeader(),teamDTO.getMembersId(),team);
        return teamMapper.entityToDto(team);
    }

    @Override
    public TeamDto findByID(Long teamID) {
        Team entity = teamRepository.getById(teamID);
        log.info("Get team by ID called. ");
        return teamMapper.entityToDto(entity);
    }

    @Override
    @Transactional
    public TeamDto delete(Long teamID) {
        Team entity = teamRepository.getById(teamID);
        revertTeamLeadToEmployee(entity);
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
