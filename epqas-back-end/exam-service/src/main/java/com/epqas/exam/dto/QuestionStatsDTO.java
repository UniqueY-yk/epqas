package com.epqas.exam.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class QuestionStatsDTO {
    private Long questionId; // 题目ID

    private Integer questionOrder; // 题目顺序

    private String questionContent; // 题目内容

    private String questionType; // 题目类型

    private String correctAnswer; // 正确答案

    private Double correctRate; // 正确率 (0~1)

    private Integer totalAnswers; // 作答人数

    private Integer correctCount; // 正确人数

    private Map<String, Integer> optionDistribution; // 各选项选择人数

    private List<String> knowledgePoints; // 关联知识点
}
