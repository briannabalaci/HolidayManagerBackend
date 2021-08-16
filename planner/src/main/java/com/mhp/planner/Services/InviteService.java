package com.mhp.planner.Services;

import com.mhp.planner.Dtos.InvitesDto;
import javassist.NotFoundException;

public interface InviteService {
    InvitesDto updateInvite(InvitesDto invitesDto) throws NotFoundException;
}
