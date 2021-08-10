package com.mhp.planner.Dtos;

import com.mhp.planner.Entities.Invite;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventDto {

    private Long id;

    private String title;

    private LocalDateTime eventDate;

    private String location;

    private String dressCode;

//    private byte[] coverImage;

    private List<InvitesDto> invites;

    private List<QuestionDto> questions;
}
