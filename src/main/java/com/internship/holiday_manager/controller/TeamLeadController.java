package com.internship.holiday_manager.controller;


import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.entity.enums.HolidayType;
import com.internship.holiday_manager.service.teamlead_service.TeamLeadService;
import com.internship.holiday_manager.service.user_service.UserService;
import com.internship.holiday_manager.utils.annotations.AllowTeamLead;
import com.internship.holiday_manager.utils.annotations.AllowTeamLeadAndEmployee;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/teamlead")
@CrossOrigin()
public class TeamLeadController {

    private final TeamLeadService teamLeadService;
    private final UserService userService;

    public TeamLeadController(TeamLeadService teamLeadService, UserService userService) {
        this.teamLeadService = teamLeadService;
        this.userService = userService;
    }

    @AllowTeamLeadAndEmployee
    @GetMapping("/requests")
    public ResponseEntity<List<HolidayDto>> getRequests(@RequestParam("id") Long id) {
        return new ResponseEntity<List<HolidayDto>>(teamLeadService.getRequests(id), HttpStatus.OK);
    }

    @AllowTeamLead
    @GetMapping("/team-requests")
    public ResponseEntity<List<HolidayDto>> getTeamRequests(@RequestParam("id") Long id) {
        return new ResponseEntity<List<HolidayDto>>(teamLeadService.getTeamRequests(id), HttpStatus.OK);
    }
    @AllowTeamLead
    @GetMapping("/getPDF")
    public ResponseEntity<Resource> download(@RequestParam("id") Long id) throws IOException, DocumentException {
        // Assume I already have this byte array from db or something


            byte[] array = teamLeadService.getPDF(id);


            ByteArrayResource resource = new ByteArrayResource(array);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resource.contentLength())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename("Team_Lead")
                                    .build().toString())
                    .body(resource);


    }


}