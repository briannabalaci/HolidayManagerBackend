package com.mhp.planner.Dtos;

import lombok.Data;

import java.util.List;

@Data
public class InvitesDto {

    private Long id;

    private String status;

    private String userInvited;

    private List<InviteQuestionResponseDto> inviteQuestionResponses;
}
