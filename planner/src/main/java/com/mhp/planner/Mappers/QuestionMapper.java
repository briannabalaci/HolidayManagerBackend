package com.mhp.planner.Mappers;

import com.mhp.planner.Dtos.QuestionDto;
import com.mhp.planner.Entities.Question;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AnswersMapper.class})
public interface QuestionMapper {

    QuestionDto entity2dto(Question question);

    Question dto2entity(QuestionDto questionDto);
}
