package com.epqas.analysis.dto.compute;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ComputeExamResultDTO {
    private Long resultId; // 成绩ID

    private Long examId; // 考试ID

    private Long studentId; // 学生ID

    private BigDecimal totalScore; // 总分

    private Boolean isAbsent; // 是否缺考
}
