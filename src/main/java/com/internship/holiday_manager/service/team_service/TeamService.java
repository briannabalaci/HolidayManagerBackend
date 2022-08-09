package com.internship.holiday_manager.service.team_service;

import com.internship.holiday_manager.dto.TeamDto;
import com.internship.holiday_manager.dto.UserDto;

import java.util.List;

public interface TeamService {
    /**
     * Save a new team entity into database
     * @param teamDTO
     * @return the added entity
     */
    TeamDto save(TeamDto teamDTO);

    /**
     * Get a team by its ID
     * @param teamID = the ID of the team
     * @return the found entity
     */
    TeamDto findByID(Long teamID);

    /**
     * Delete a team entity
     * @param teamID = the ID of the entity you want to delete
     * @return the deleted entity
     */
    TeamDto delete(Long teamID);

    /**
     * Get all teams from database
     * @return a list with all the teams
     */
    List<TeamDto> getAllTeams();

}
