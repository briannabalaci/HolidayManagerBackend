package com.mhp.planner.Services;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Dtos.UserDto;

import java.util.List;

public interface EventService {


    List<EventDto> getAllEvents();
    EventDto addEvent(EventDto eventDto);
}
