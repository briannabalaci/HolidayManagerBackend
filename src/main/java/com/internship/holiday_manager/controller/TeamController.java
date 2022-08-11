package com.internship.holiday_manager.controller;

import com.internship.holiday_manager.dto.TeamDto;
import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.service.team_service.TeamService;
import com.internship.holiday_manager.utils.annotations.AllowAdmin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
@CrossOrigin
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @AllowAdmin
    @PostMapping("/add")
    public ResponseEntity<TeamDto> save(@RequestBody TeamDto entityDto){
        return new ResponseEntity<>(teamService.save(entityDto), HttpStatus.OK);
    }

    @AllowAdmin
    @DeleteMapping("/delete/{teamID}")
    public ResponseEntity<TeamDto> delete(@PathVariable long teamID){
        return new ResponseEntity<>(teamService.delete(teamID),HttpStatus.OK);
    }

    @AllowAdmin
    @GetMapping("/get-all")
    public ResponseEntity<List<TeamDto>> getAll(){
        return new ResponseEntity<>(teamService.getAllTeams(),HttpStatus.OK);
    }

    @AllowAdmin
    @GetMapping("/get-by-id/{teamID}")
    public ResponseEntity<TeamDto> getById(@PathVariable long teamID){
        TeamDto entity = teamService.findByID(teamID);
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @AllowAdmin
    @GetMapping("/get-members/{teamID}")
    public ResponseEntity<List<UserDto>> getTeamMembers(@PathVariable long teamID){
        return new ResponseEntity<>(teamService.findByID(teamID).getMembers(),HttpStatus.OK);
    }
}
