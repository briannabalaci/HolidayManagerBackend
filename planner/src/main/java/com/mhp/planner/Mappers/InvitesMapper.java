package com.mhp.planner.Mappers;

import com.mhp.planner.Dtos.InvitesDto;
import com.mhp.planner.Entities.Invite;
import com.mhp.planner.Repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {InviteQuestionResponseMapper.class, QuestionMapper.class, UserRepository.class, UserMapper.class, AnswersMapper.class})
public interface InvitesMapper {

    @Mapping(target="userInvited", expression = "java(invite.getUserInvited().getEmail())")
    InvitesDto entity2dto(Invite invite);

    List<InvitesDto> entities2dtos(List<Invite> invitesList);

    List<Invite> dtos2entities(List<InvitesDto> invitesDtos);

    Invite dto2entity(InvitesDto invitesDto);
}
