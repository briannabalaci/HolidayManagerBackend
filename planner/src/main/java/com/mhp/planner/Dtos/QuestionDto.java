package com.mhp.planner.Dtos;

import com.mhp.planner.Entities.Question;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {

    private Long id;

    private String text;

    private List<AnswersDto> answerList;

    @Override
    public boolean equals(Object e)
    {
        if(e instanceof QuestionDto)
        {
            QuestionDto question = (QuestionDto) e;
            return question.getId().equals(this.id);
        }
        return false;
    }
}
