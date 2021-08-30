package com.mhp.planner.Services;

import com.mhp.planner.Dtos.AnswersDto;
import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Entities.Event;
import com.mhp.planner.Entities.Invite;
import com.mhp.planner.Entities.Question;
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
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
    private final EmailService emailService;

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
            Event event = eventOptional.get();
            event.setQuestions(event.getQuestions().stream().sorted(Comparator.comparingLong(Question::getId)).collect(Collectors.toList()));
            return eventMapper.entity2dto(eventOptional.get());
        }
    }

    @Override
    public EventDto createEvent(EventDto eventDto) throws IOException {
        Event event = eventMapper.dto2entity(eventDto);
        System.out.println(event);
        Event createdEvent = eventRepository.save(event);

        List<String> inviteEmails = createdEvent.getInvites().stream()
                .map(Invite::getUserInvited)
                .map(User::getEmail)
                .collect(Collectors.toList());

        Date date = Date.from(createdEvent.getEventDate().atZone(ZoneId.systemDefault()).toInstant());

        emailService.sendTemplatedEmail(
                "New MHP event invitation",
                "newEventTemplate.html",
                Map.of("eventName", createdEvent.getTitle(),
                        "location", createdEvent.getLocation(),
                        "date", date.toString(),
                        "dressCode", createdEvent.getDressCode(),
                        "id", createdEvent.getId().toString()), inviteEmails);

        return eventMapper.entity2dto(createdEvent);
    }

    @Override
    public void deleteEvent(Long id) throws NotFoundException {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isEmpty()) {
            throw new NotFoundException("Event with id " + id + " not found!");
        }

        Event event = eventOptional.get();
        List<String> inviteEmails = event.getInvites().stream()
                .map(Invite::getUserInvited)
                .map(User::getEmail)
                .collect(Collectors.toList());

        eventRepository.deleteById(id);

        emailService.sendTemplatedEmail(
                "MHP event cancellation",
                "cancellationEventTemplate.html",
                Map.of("eventName", event.getTitle()),
                inviteEmails);
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

            if (!event.getEventDate().isEqual(eventDto.getEventDate())) {
                event.getInvites().forEach(
                        s -> {
                            s.setStatus("pending");
                            event.getQuestions().forEach(e -> inviteQuestionRepository.deleteByQuestion_Id(e.getId()));
                        });

            }
            event.setEventDate(eventDto.getEventDate());
            event.setLocation(eventDto.getLocation());
            event.setDressCode(eventDto.getDressCode());

            //set invites
            for (var invite : eventDto.getInvites()) {
                if (invite.getId() == null) {
                    event.getInvites().add(invitesMapper.dto2entity(invite));
                }
            }

            //set questions
            for (var question : event.getQuestions()) {
                if (!eventDto.getQuestions().contains(questionMapper.entity2dto(question))) {
                    inviteQuestionRepository.deleteByQuestion_Id(question.getId());
                    System.out.println(inviteQuestionRepository.findAll());
                    questionRepository.deleteById(question.getId());
                }
            }
            var nullIds = eventDto.getQuestions().stream().filter(s -> s.getId() == null).collect(Collectors.toList());
            if (nullIds.size() != 0) {

                event.getInvites().forEach(s -> {
                    s.setStatus("pending");
                    event.getQuestions().forEach(e -> {
                        inviteQuestionRepository.deleteByQuestion_Id(e.getId());
                    });
                });

            }



            event.getQuestions().clear();

            event.getQuestions().addAll(questionMapper.dtos2entities(eventDto.getQuestions()));

            Event updatedEntity = eventRepository.save(event);

            return eventMapper.entity2dto(updatedEntity);
        }
    }



}
