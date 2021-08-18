package com.mhp.planner.Dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mhp.planner.Entities.Invite;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventDto {

    private Long id;

    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private String location;

    private String dressCode;

    private String cover_image;

    private List<InvitesDto> invites;

    private List<QuestionDto> questions;

    private String organizer;

    private int time_limit;
}
