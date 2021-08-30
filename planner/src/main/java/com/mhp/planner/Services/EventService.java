package com.mhp.planner.Services;

import com.mhp.planner.Dtos.AnswersDto;
import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Dtos.QuestionDto;
import com.mhp.planner.Entities.Answers;
import com.mhp.planner.Entities.Event;
import javassist.NotFoundException;
import java.io.IOException;
import java.util.List;

public interface EventService {

    List<EventDto> getAllEvents();

    EventDto getEvent(Long id) throws NotFoundException;


    EventDto createEvent(EventDto eventDto) throws IOException;

    void deleteEvent(Long id) throws NotFoundException;

    List<EventDto> getEventsBy(String email, String filter);

    EventDto updateEvent(EventDto eventDto) throws NotFoundException;


}
