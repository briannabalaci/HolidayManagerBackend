package com.mhp.planner.Controller;

import com.mhp.planner.Dtos.InviteQuestionResponseDto;
import com.mhp.planner.Dtos.InvitesDto;
import com.mhp.planner.Services.InviteService;
import com.mhp.planner.Util.Annotations.AllowNormalUser;
import com.mhp.planner.Util.Annotations.AllowOrganizer;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invites")
@AllArgsConstructor
@CrossOrigin
public class InviteController {
    private final InviteService inviteService;

    @AllowNormalUser
    @PutMapping("/update")
    public ResponseEntity<InvitesDto> updateInvite(@RequestBody InvitesDto invitesDto) throws NotFoundException {
        return ResponseEntity.ok(inviteService.updateInvite(invitesDto));
    }

    @AllowNormalUser
    @GetMapping("/getResponses/{id}")
    public ResponseEntity<List<InviteQuestionResponseDto>> getResponses(@PathVariable("id") Long id) throws NotFoundException {
        return ResponseEntity.ok(inviteService.getQuestionResponseById(id));
    }

    @AllowOrganizer
    @GetMapping("/getByStatus/{status}")
    public ResponseEntity<List<InvitesDto>> getByStatus(@PathVariable("status") String status) {
        return ResponseEntity.ok(inviteService.findByStatus(status));
    }
}
