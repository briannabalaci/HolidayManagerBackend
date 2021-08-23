package com.mhp.planner.Controller;

import com.itextpdf.text.Document;
import com.mhp.planner.Dtos.EventDto;
import com.mhp.planner.Dtos.QuestionDto;
import com.mhp.planner.Services.EventService;
import com.mhp.planner.Services.StatisticsService;
import com.mhp.planner.Util.Annotations.AllowNormalUser;
import com.mhp.planner.Util.Annotations.AllowOrganizer;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/event")
@AllArgsConstructor
@CrossOrigin
public class EventController {

    private final EventService eventService;
    private final StatisticsService statisticsService;

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
    @PutMapping("/updateEvent")
    public ResponseEntity<EventDto> updateEvent(@RequestBody EventDto eventDto) throws NotFoundException {
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


    @AllowOrganizer
    @GetMapping("{id}/statistics/{filter}")
    public ResponseEntity<?> getStatisticsByFilter(@PathVariable("id") Long id, @PathVariable("filter") String filter) {
        ByteArrayInputStream pdf = statisticsService.generatePDFByFilter(id, filter);
        if(pdf == null) {
            return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
        }
        else {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", String.format("inline; filename=event%2d.pdf", id));

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdf));
        }
    }

}
