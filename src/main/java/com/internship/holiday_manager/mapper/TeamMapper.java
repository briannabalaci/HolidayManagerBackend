
package com.internship.holiday_manager.mapper;
import com.internship.holiday_manager.dto.TeamDto;
import com.internship.holiday_manager.entity.Team;
import org.mapstruct.*;
import java.util.List;
@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface TeamMapper {

    TeamDto entityToDto(Team entity);

    Team dtoToEntity(TeamDto dto);

    List<TeamDto> entitiesToDtos(List<Team> entities);

    List<Team> dtosToEntities(List<TeamDto> dtos);
}



