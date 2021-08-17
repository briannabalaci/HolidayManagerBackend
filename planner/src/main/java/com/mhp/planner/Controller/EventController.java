package com.mhp.planner.Controller;

import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Services.EventService;
import com.mhp.planner.Util.Annotations.AllowNormalUser;
import com.mhp.planner.Util.Annotations.AllowOrganizer;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @AllowNormalUser
    @GetMapping("/getAll")
    public ResponseEntity<List<EventDto>> getAllEvents()
    {
        List<EventDto> eventDtos = eventService.getAllEvents();
        System.out.println(eventDtos);

        return ResponseEntity.ok(eventDtos);
    }

    @AllowNormalUser
    @GetMapping("{id}")
    public ResponseEntity<?> getEvent(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(eventService.getEvent(id));
        }
        catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @AllowOrganizer
    @PostMapping(value = "/addEvent")
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) throws IOException {
        return ResponseEntity.ok(eventService.createEvent(eventDto));
    }

    @AllowOrganizer
    @PutMapping(value = "/updateEvent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventDto> updateEvent(@RequestPart EventDto eventDto, @RequestPart MultipartFile file) throws IOException, NotFoundException {
        byte[] cover_image = file.getBytes();
        eventDto.setCover_image(cover_image);
        return ResponseEntity.ok(eventService.updateEvent(eventDto));
    }

    @AllowOrganizer
    @DeleteMapping("/deleteEvent/{id}")
    public ResponseEntity<HttpStatus> deleteEvent(@PathVariable("id") Long id) {
        try {
            eventService.deleteEvent(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


//    @AllowNormalUser
//    @GetMapping("/getImage/{id}")
//    public ResponseEntity<byte[]> getEventImage (@PathVariable Long id) {
//        return ResponseEntity.ok(this.eventService.getImageBasedOnEvent(id).getCover_image());
//    }

    @AllowNormalUser
    @GetMapping("/getAllBy")
    public ResponseEntity<List<EventDto>> getEventsByIdAndFilter(@RequestParam("email") String email, @RequestParam(name="filter") String filter) {
        return ResponseEntity.ok(eventService.getEventsBy(email, filter));
    }
}
