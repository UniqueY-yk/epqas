package com.epqas.common.dto;

import lombok.Data;

@Data
public class QuestionBatchDTO {
    private Long questionId;
    private String questionContent;
    private String questionType;
    private String optionsJson;
    private String correctAnswer;
}
