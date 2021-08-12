package com.mhp.planner.Controller;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Dtos.UserDto;
import com.mhp.planner.Services.EventService;
import com.mhp.planner.Util.Annotations.AllowAttendee;
import com.mhp.planner.Util.Annotations.AllowOrganizer;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/event")
@AllArgsConstructor
@CrossOrigin
public class EventController {

    private final EventService eventService;

    @AllowOrganizer
    @AllowAttendee
    @GetMapping("/getAll")
    public ResponseEntity<List<EventDto>> getAllEvents()
    {
        List<EventDto> eventDtos = eventService.getAllEvents();
        System.out.println(eventDtos);

        return ResponseEntity.ok(eventDtos);
    }

    @AllowOrganizer
    @PostMapping(value = "/addEvent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventDto> createEvent(@RequestPart EventDto eventDto, @RequestPart MultipartFile file) throws IOException {
        byte[] cover_image = file.getBytes();
        eventDto.setCover_image(cover_image);
        return ResponseEntity.ok(eventService.createEvent(eventDto));
    }


    @AllowOrganizer
    @AllowAttendee
    @GetMapping("/getImage/{id}")
    public ResponseEntity<byte[]> getEventImage (@PathVariable Long id) {
        return ResponseEntity.ok(this.eventService.getImageBasedOnEvent(id).getCover_image());
    }

    @AllowOrganizer
    @AllowAttendee
    @GetMapping("/getAllBy")
    public ResponseEntity<List<EventDto>> getEventsByIdAndFilter(@RequestParam("email") String email, @RequestParam(name="filter") String filter) {
        return ResponseEntity.ok(eventService.getEventsBy(email, filter));
    }
}
