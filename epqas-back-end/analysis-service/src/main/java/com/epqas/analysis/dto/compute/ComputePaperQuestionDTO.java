package com.epqas.analysis.dto.compute;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ComputePaperQuestionDTO {
    private Long paperId; // 试卷ID

    private Long questionId; // 题目ID

    private BigDecimal scoreValue; // 分值

    private Integer questionOrder; // 题目顺序

    private Float initialDifficulty; // 初始难度
}
