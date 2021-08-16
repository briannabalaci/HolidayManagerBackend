package com.mhp.planner.Mappers;

import com.mhp.planner.Dtos.InviteQuestionResponseDto;
import com.mhp.planner.Entities.InviteQuestionResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class, AnswersMapper.class})
public interface InviteQuestionResponseMapper {
    InviteQuestionResponseDto entity2dto(InviteQuestionResponse entity);

    InviteQuestionResponse dto2entity(InviteQuestionResponseDto dto);

    List<InviteQuestionResponseDto> entities2dtos(List<InviteQuestionResponse> entities);

    List<InviteQuestionResponse> dto2entities(List<InviteQuestionResponseDto> dtos);
}
