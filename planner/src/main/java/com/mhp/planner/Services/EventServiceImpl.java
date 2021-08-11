
package com.mhp.planner.Services;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Entities.Event;
import com.mhp.planner.Entities.User;
import com.mhp.planner.Mappers.EventMapper;
import com.mhp.planner.Repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService{

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public List<EventDto> getAllEvents() {

        List<Event> eventList = eventRepository.findAll();

        System.out.println(eventList);

        return eventMapper.entities2dtos(eventList);
    }

    @Override
    public EventDto createEvent(EventDto eventDto) throws IOException {

        Event event = eventMapper.dto2entity(eventDto);
        System.out.println(event);
        Event createdEvent = eventRepository.save(event);
        return eventMapper.entity2dto(createdEvent);
    }

    public List<EventDto> getEventsBy(String filter) {
        switch (filter) {
            case "all_events": {
                List<Event> events = eventRepository.findAll();
                return eventMapper.entities2dtos(events);
            }
            case "future_events": {
                List<Event> events = eventRepository.findAllByEventDateAfter(LocalDateTime.now());
                return eventMapper.entities2dtos(events);
            }
            case "accepted": {
//                List<Event> events
            }
        }
        return null;
    }

}
