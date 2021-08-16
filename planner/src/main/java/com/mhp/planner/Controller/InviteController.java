package com.mhp.planner.Controller;

import com.mhp.planner.Dtos.InvitesDto;
import com.mhp.planner.Services.InviteService;
import com.mhp.planner.Util.Annotations.AllowAll;
import com.mhp.planner.Util.Annotations.AllowNormalUser;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invites")
@AllArgsConstructor
@CrossOrigin
public class InviteController {
    private final InviteService inviteService;

    //@AllowAll
    @PutMapping("/update")
    public ResponseEntity<InvitesDto> updateInvite(@RequestBody InvitesDto invitesDto) throws NotFoundException {
        return ResponseEntity.ok(inviteService.updateInvite(invitesDto));
    }
}
