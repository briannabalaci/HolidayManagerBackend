package com.mhp.planner.Mappers;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AnswersMapper.class})
public interface QuestionMapper {
}
