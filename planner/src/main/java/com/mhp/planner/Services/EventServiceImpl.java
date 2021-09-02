package com.mhp.planner.Services;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Dtos.InvitesDto;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

        LocalDateTime eventDate = createdEvent.getEventDate();

        String date = String.format("%02d/%02d/%04d",
                eventDate.getDayOfMonth(),
                eventDate.getMonthValue(),
                eventDate.getYear());

        String pattern = "hh:mm a";
        String time = eventDate.format(DateTimeFormatter.ofPattern(pattern));

        for (Invite invites : createdEvent.getInvites()) {
            User invitedUser = invites.getUserInvited();

            emailService.sendTemplatedEmail(
                    "New MHP event invitation",
                    "newEventTemplate.html",
                    Map.of("eventName", createdEvent.getTitle(),
                            "foreName", invitedUser.getForename(),
                            "location", createdEvent.getLocation(),
                            "date", date,
                            "time", time,
                            "dressCode", createdEvent.getDressCode(),
                            "id", createdEvent.getId().toString()),
                    invitedUser.getEmail());
        }

        return eventMapper.entity2dto(createdEvent);
    }

    @Override
    public void deleteEvent(Long id) throws NotFoundException {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isEmpty()) {
            throw new NotFoundException("Event with id " + id + " not found!");
        }

        Event event = eventOptional.get();

        eventRepository.deleteById(id);

        for (Invite invites : event.getInvites()) {
            User invitedUser = invites.getUserInvited();

            emailService.sendTemplatedEmail(
                    "MHP event cancellation",
                    "cancellationEventTemplate.html",
                    Map.of("eventName", event.getTitle(),
                            "foreName", invitedUser.getForename()),
                    invitedUser.getEmail());
        }
    }

    public List<EventDto> getEventsBy(String email, String filter) {
        switch (filter) {
            case "All Events": {
                List<Event> events = eventRepository.findAllByInvites_UserInvited_Email(email);
                return eventMapper.entities2dtos(events).stream().sorted(Comparator.comparingLong(EventDto::getId).reversed()).collect(Collectors.toList());
            }
            case "Future Events": {
                List<Event> events = eventRepository.findAllByInvites_UserInvited_EmailAndEventDateAfter(email, LocalDateTime.now());
                return eventMapper.entities2dtos(events).stream().sorted(Comparator.comparingLong(EventDto::getId).reversed()).collect(Collectors.toList());
            }
            case "Accepted": {
                List<Event> events = eventRepository.findAllByInvites_UserInvited_EmailAndInvites_Status(email, "accepted");
                return eventMapper.entities2dtos(events).stream().sorted(Comparator.comparingLong(EventDto::getId).reversed()).collect(Collectors.toList());
            }
            case "Declined": {
                List<Event> events = eventRepository.findAllByInvites_UserInvited_EmailAndInvites_Status(email, "declined");
                return eventMapper.entities2dtos(events).stream().sorted(Comparator.comparingLong(EventDto::getId).reversed()).collect(Collectors.toList());
            }
            case "My Events": {
                List<Event> events = eventRepository.findAllByOrganizer_Email(email);
                return eventMapper.entities2dtos(events).stream().sorted(Comparator.comparingLong(EventDto::getId).reversed()).collect(Collectors.toList());
            }
        }
        return null;
    }

    @Override
    @Transactional
    public EventDto updateEvent(EventDto eventDto) throws NotFoundException {
        Optional<Event> eventOptional = eventRepository.findById(eventDto.getId());

        boolean sendUpdate = false;

        if (eventOptional.isEmpty()) {
            throw new NotFoundException("Event with id " + eventDto.getId() + " not found!");
        } else {
            Event event = eventOptional.get();
            System.out.println(eventDto);

            event.setTitle(eventDto.getTitle());

            if (!event.getEventDate().isEqual(eventDto.getEventDate())) {
                sendUpdate = true;
                event.getInvites().forEach(
                        s -> {
                            s.setStatus("pending");
                            event.getQuestions().forEach(e -> inviteQuestionRepository.deleteByQuestion_Id(e.getId()));
                        });


            }
            event.setEventDate(eventDto.getEventDate());
            event.setLocation(eventDto.getLocation());
            event.setDressCode(eventDto.getDressCode());
            event.setTime_limit(eventDto.getTime_limit());

            LocalDateTime eventDate = eventDto.getEventDate();

            String date = String.format("%02d/%02d/%04d",
                    eventDate.getDayOfMonth(),
                    eventDate.getMonthValue(),
                    eventDate.getYear());

            String pattern = "hh:mm a";
            String time = eventDate.format(DateTimeFormatter.ofPattern(pattern));

            //set invites
            for (var invite : eventDto.getInvites()) {
                if (invite.getId() == null) {
                    event.getInvites().add(invitesMapper.dto2entity(invite));
                    emailService.sendTemplatedEmail(
                            "New MHP event invitation",
                            "newEventTemplate.html",
                            Map.of("eventName", eventDto.getTitle(),
                                    "foreName", userRepository.findByEmail(invite.getUserInvited()).getForename(),
                                    "location", eventDto.getLocation(),
                                    "date", date,
                                    "time", time,
                                    "dressCode", eventDto.getDressCode(),
                                    "id", eventDto.getId().toString()),
                            invite.getUserInvited());
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
                sendUpdate = true;
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


            if (sendUpdate) {
                for (InvitesDto invites : eventDto.getInvites()) {
                    String invitedUser = invites.getUserInvited();

                    if(invites.getId() != null) {
                        emailService.sendTemplatedEmail(
                                "MHP event update",
                                "updateEventTemplate.html",
                                Map.of("eventName", updatedEntity.getTitle(),
                                        "foreName", userRepository.findByEmail(invitedUser).getForename(),
                                        "location", updatedEntity.getLocation(),
                                        "date", date,
                                        "time", time,
                                        "dressCode", updatedEntity.getDressCode(),
                                        "id", updatedEntity.getId().toString()),
                                invitedUser);
                    }
                }
            }
            return eventMapper.entity2dto(updatedEntity);
        }
    }



}
