package com.mhp.planner.Services;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Dtos.UserDto;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.List;

public interface EventService {


    List<EventDto> getAllEvents();

    EventDto getEvent(Long id) throws NotFoundException;

    EventDto getImageBasedOnEvent(Long id);

    EventDto createEvent(EventDto eventDto) throws IOException;

    List<EventDto> getEventsBy(String email, String filter);
}
