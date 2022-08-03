package com.internship.holiday_manager.mapper;

import com.internship.holiday_manager.dto.TeamDto;
import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.Team;
import com.internship.holiday_manager.entity.User;
import org.mapstruct.Mapper;

import java.util.List;
@Mapper(componentModel = "spring", uses = Team.class)
public interface TeamMapper {

    TeamDto entityToDto(Team entity);

    Team dtoToEntity(TeamDto dto);

    List<TeamDto> entitiesToDtos(List<Team> entities);

    List<Team> dtosToEntities(List<TeamDto> dtos);

}
