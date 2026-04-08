package com.epqas.analysis.dto.compute;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ComputeStudentAnswerDTO {
    private Long answerId; // 答案ID

    private Long resultId; // 成绩ID

    private Long questionId; // 题目ID

    private Long studentId; // 学生ID

    private BigDecimal scoreObtained; // 获得分数

    private Boolean isCorrect; // 是否正确

    private String studentChoice; // 学生选项
}
