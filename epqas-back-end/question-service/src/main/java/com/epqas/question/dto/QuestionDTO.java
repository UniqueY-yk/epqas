package com.epqas.question.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long questionId;
    private Integer courseId;
    private Long creatorId;
    private String questionContent;
    private String questionType;
    private String optionsJson;
    private String correctAnswer;
    private Float initialDifficulty;
    private List<Integer> pointIds;
}
