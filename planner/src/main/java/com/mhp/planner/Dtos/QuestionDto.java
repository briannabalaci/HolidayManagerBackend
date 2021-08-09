package com.mhp.planner.Dtos;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {

    private long id;

    private String text;

    private List<AnswersDto> answers;
}
