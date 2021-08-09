package com.mhp.planner.Mappers;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {InvitesMapper.class, QuestionMapper.class})
public interface EventMapper {
}
