package com.mhp.planner.Services;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Dtos.QuestionDto;
import com.mhp.planner.Entities.Event;
import com.mhp.planner.Entities.Invite;
import com.mhp.planner.Entities.InviteQuestionResponse;
import com.mhp.planner.Entities.User;
import com.mhp.planner.Mappers.EventMapper;
import com.mhp.planner.Mappers.InvitesMapper;
import com.mhp.planner.Mappers.QuestionMapper;
import com.mhp.planner.Repository.*;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final InvitesRepository invitesRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final EventMapper eventMapper;
    private final InvitesMapper invitesMapper;
    private final QuestionMapper questionMapper;
    private final InviteQuestionRepository inviteQuestionRepository;

    @Override
    public List<EventDto> getAllEvents() {

        List<Event> eventList = eventRepository.findAll();

        System.out.println(eventList);

        return eventMapper.entities2dtos(eventList);
    }

    @Override
    public EventDto getEvent(Long id) throws NotFoundException {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isEmpty()) {
            throw new NotFoundException("Event with id " + id + " not found");
        } else {
            return eventMapper.entity2dto(eventOptional.get());
        }
    }

    @Override
    public EventDto getImageBasedOnEvent(Long id) {
        Event event = eventRepository.findById(id).get();
        return eventMapper.entity2dto(event);
    }

    @Override
    public EventDto createEvent(EventDto eventDto) throws IOException {

        Event event = eventMapper.dto2entity(eventDto);
        System.out.println(event);
        Event createdEvent = eventRepository.save(event);
        return eventMapper.entity2dto(createdEvent);
    }

    @Override
    public void deleteEvent(Long id) throws NotFoundException {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if(eventOptional.isEmpty()) {
            throw new NotFoundException("Event with id " + id + " not found!");
        }
        else {
            eventRepository.deleteById(id);
        }
    }

    public List<EventDto> getEventsBy(String email, String filter) {
        switch (filter) {
            case "All Events": {
                List<Event> events = eventRepository.findAllByInvites_UserInvited_Email(email);
                return eventMapper.entities2dtos(events);
            }
            case "Future Events": {
                List<Event> events = eventRepository.findAllByInvites_UserInvited_EmailAndEventDateAfter(email, LocalDateTime.now());
                return eventMapper.entities2dtos(events);
            }
            case "Accepted": {
                List<Event> events = eventRepository.findAllByInvites_UserInvited_EmailAndInvites_Status(email, "accepted");
                return eventMapper.entities2dtos(events);
            }
            case "Declined": {
                List<Event> events = eventRepository.findAllByInvites_UserInvited_EmailAndInvites_Status(email, "declined");
                return eventMapper.entities2dtos(events);
            }
            case "My Events": {
                List<Event> events = eventRepository.findAllByOrganizer_Email(email);
                return eventMapper.entities2dtos(events);
            }
        }
        return null;
    }

    @Override
    @Transactional
    public EventDto updateEvent(EventDto eventDto) throws NotFoundException {
        Optional<Event> eventOptional = eventRepository.findById(eventDto.getId());

        if (eventOptional.isEmpty()) {
            throw new NotFoundException("Event with id " + eventDto.getId() + " not found!");
        } else {
            Event event = eventOptional.get();
            System.out.println(eventDto);

            event.setTitle(eventDto.getTitle());

            if(!event.getEventDate().isEqual(eventDto.getEventDate())) {
                for(Invite invite : event.getInvites()) {
                    invite.setInviteQuestionResponses(new ArrayList<InviteQuestionResponse>());
                    invite.setStatus("pending");
                }
            }
            event.setEventDate(eventDto.getEventDate());
            event.setLocation(eventDto.getLocation());
            event.setDressCode(eventDto.getDressCode());

            //set invites
            for(var invite: eventDto.getInvites())
            {
                    if(invite.getId() == null)
                    {
                        event.getInvites().add(invitesMapper.dto2entity(invite));
                    }
            }

            //set questions
            for(var question: event.getQuestions())
            {
                if(!eventDto.getQuestions().contains(questionMapper.entity2dto(question)))
                {
                    inviteQuestionRepository.deleteByQuestion_Id(question.getId());
                    System.out.println(inviteQuestionRepository.findAll());
                    questionRepository.deleteById(question.getId());
                }
            }
            event.getQuestions().clear();
            event.getQuestions().addAll(questionMapper.dtos2entities(eventDto.getQuestions()));

            Event updatedEntity = eventRepository.save(event);

            return eventMapper.entity2dto(updatedEntity);
        }
    }

}
