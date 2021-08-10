package com.mhp.planner.Mappers;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Entities.Event;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {InvitesMapper.class, QuestionMapper.class})
public interface EventMapper {

    EventDto entity2dto(Event event);

    List<EventDto> entities2dtos (List<Event> event);
}
