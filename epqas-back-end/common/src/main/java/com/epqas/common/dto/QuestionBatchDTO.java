package com.epqas.common.dto;

import lombok.Data;

@Data
public class QuestionBatchDTO {
    private Long questionId; // 题目ID

    private String questionContent; // 题目内容

    private String questionType; // 题目类型

    private String optionsJson; // 选项JSON

    private String correctAnswer; // 正确答案
}
