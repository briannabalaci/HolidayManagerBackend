package com.mhp.planner.Dtos;

import lombok.Data;

import java.util.List;

@Data
public class InvitesDto {

    private long id;

    private String status;

    private List<InviteQuestionResponseDto> inviteQuestionResponse;
}