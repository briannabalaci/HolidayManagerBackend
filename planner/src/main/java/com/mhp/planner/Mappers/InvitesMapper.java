package com.mhp.planner.Mappers;

import com.mhp.planner.Dtos.InvitesDto;
import com.mhp.planner.Entities.Invite;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {InviteQuestionResponseMapper.class, QuestionMapper.class})
public interface InvitesMapper {

    InvitesDto entity2dto(Invite invite);

    Invite dto2entity(InvitesDto invitesDto);
}
