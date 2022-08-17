package com.internship.holiday_manager.controller;

import com.internship.holiday_manager.dto.team.TeamAddDto;
import com.internship.holiday_manager.dto.team.TeamDto;
import com.internship.holiday_manager.dto.team.TeamUpdateDto;
import com.internship.holiday_manager.dto.user.UserWithTeamIdDto;
import com.internship.holiday_manager.service.team_service.TeamService;
import com.internship.holiday_manager.utils.annotations.AllowAdmin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = "localhost:4200")
@Slf4j
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @AllowAdmin
    @PostMapping("/add")
    public ResponseEntity<TeamDto> save(@RequestBody TeamAddDto entityDto) throws Exception {
        try {
            return new ResponseEntity<>(teamService.save(entityDto), HttpStatus.OK);
        }catch(Exception e){
            log.info("Intra pe catch");
            return new ResponseEntity<>(new TeamDto(),HttpStatus.OK);
        }
    }
    @AllowAdmin
    @PutMapping("/update")
    public ResponseEntity<TeamDto> update(@RequestBody TeamUpdateDto entityDto) throws Exception {
        try {
            return new ResponseEntity<>(teamService.update(entityDto), HttpStatus.OK);
        }catch(Exception e){
            log.info("Intra pe catch");
            return new ResponseEntity<>(new TeamDto(),HttpStatus.OK);
        }
    }

    @AllowAdmin
    @DeleteMapping("/delete/{teamID}")
    public ResponseEntity<TeamDto> delete(@PathVariable long teamID){
        return new ResponseEntity<>(teamService.delete(teamID),HttpStatus.OK);
    }

    @AllowAdmin
    @GetMapping("/all")
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
    @GetMapping("/members/{teamID}")
    public ResponseEntity<List<UserWithTeamIdDto>> getTeamMembers(@PathVariable long teamID){
        return new ResponseEntity<>(teamService.findByID(teamID).getMembers(),HttpStatus.OK);
    }
}
