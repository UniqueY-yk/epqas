package com.epqas.analysis.dto.compute;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ComputeExamResultDTO {
    private Long resultId;
    private Long examId;
    private Long studentId;
    private BigDecimal totalScore;
    private Boolean isAbsent;
}
