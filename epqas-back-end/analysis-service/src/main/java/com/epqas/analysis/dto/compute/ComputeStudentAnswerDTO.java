package com.epqas.analysis.dto.compute;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ComputeStudentAnswerDTO {
    private Long answerId;
    private Long resultId;
    private Long questionId;
    private Long studentId;
    private BigDecimal scoreObtained;
    private Boolean isCorrect;
    private String studentChoice;
}
