package com.mhp.planner.Mappers;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Entities.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.io.IOException;
import java.util.List;

@Mapper(componentModel = "spring", uses = {InvitesMapper.class, QuestionMapper.class, UserMapper.class})
public interface EventMapper {

    EventDto entity2dto(Event event);

//    @Mapping(target="cover_image", expression = "java(eventDto.getCover_image().getBytes())")
    Event dto2entity(EventDto eventDto) throws IOException;

    List<EventDto> entities2dtos (List<Event> event);

}
