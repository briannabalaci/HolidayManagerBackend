package com.mhp.planner.Services;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Dtos.UserDto;

import java.io.IOException;
import java.util.List;

public interface EventService {


    List<EventDto> getAllEvents();

    EventDto createEvent(EventDto eventDto) throws IOException;

    List<EventDto> getEventsBy(Long id, String filter);
}
