package com.mhp.planner.Mappers;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {InviteQuestionResponseMapper.class})
public interface InvitesMapper {
}
