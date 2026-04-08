package com.epqas.exam.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaperQuestionDTO {
    private Long questionId; // 题目ID

    private BigDecimal scoreValue; // 分值

    private Integer questionOrder; // 题目顺序

    private String questionContent; // 题目内容

    private String questionType; // 题目类型

    private String optionsJson; // 选项JSON

    private String correctAnswer; // 正确答案
}
