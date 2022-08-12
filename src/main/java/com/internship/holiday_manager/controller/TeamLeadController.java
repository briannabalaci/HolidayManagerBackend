package com.internship.holiday_manager.controller;


import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.user.UserDto;
import com.internship.holiday_manager.entity.enums.HolidayType;
import com.internship.holiday_manager.service.teamlead_service.TeamLeadService;
import com.internship.holiday_manager.service.user_service.UserService;
import com.internship.holiday_manager.utils.annotations.AllowTeamLead;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @AllowTeamLead
    @GetMapping("/user")
    public ResponseEntity<UserDto> getUser(@RequestParam("email") String email){
        return new ResponseEntity<>(this.userService.getUser(email), HttpStatus.OK);
    }

    @AllowTeamLead
    @GetMapping("/requests")
    public ResponseEntity<List<HolidayDto>> getRequests(@RequestParam("id") Long id){
        return new ResponseEntity<List<HolidayDto>>(teamLeadService.getRequests(id), HttpStatus.OK);
    }

    @AllowTeamLead
    @GetMapping("/team-requests")
    public ResponseEntity<List<HolidayDto>> getTeamRequests(@RequestParam("id") Long id){
        return new ResponseEntity<List<HolidayDto>>(teamLeadService.getTeamRequests(id), HttpStatus.OK);
    }

    @AllowTeamLead
    @GetMapping("/requests-filtered-by")
    public ResponseEntity<List<HolidayDto>> getRequestsFilteredByType(@RequestParam("type") HolidayType type, @RequestParam("id") Long id){
        return new ResponseEntity<>(teamLeadService.getRequestsByType(id, type), HttpStatus.OK);
    }

}
