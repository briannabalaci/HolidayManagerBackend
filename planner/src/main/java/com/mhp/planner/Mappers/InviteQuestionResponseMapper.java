package com.mhp.planner.Mappers;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class, AnswersMapper.class})
public interface InviteQuestionResponseMapper {


}
