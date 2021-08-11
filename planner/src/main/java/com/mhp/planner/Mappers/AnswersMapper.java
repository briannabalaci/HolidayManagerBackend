package com.mhp.planner.Mappers;

import com.mhp.planner.Dtos.AnswersDto;
import com.mhp.planner.Entities.Answers;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnswersMapper {

    AnswersDto entity2dto(Answers answer);

    Answers dto2entity(AnswersDto answersDto);
}
