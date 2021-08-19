package com.mhp.planner.Services;

import com.mhp.planner.Dtos.InvitesDto;
import javassist.NotFoundException;

import java.util.List;

public interface InviteService {
    InvitesDto updateInvite(InvitesDto invitesDto) throws NotFoundException;
    List<InvitesDto> findByStatus(String status);
}
