package com.mhp.planner.Dtos;

import lombok.Data;

@Data
public class InviteQuestionResponseDto {

    private long id;

    private QuestionDto question;

    private AnswersDto answer;
}
