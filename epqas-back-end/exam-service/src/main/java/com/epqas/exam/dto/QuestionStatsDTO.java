package com.epqas.exam.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class QuestionStatsDTO {
    private Long questionId;
    private Integer questionOrder;
    private String questionContent;
    private String questionType;
    private String correctAnswer;
    private Double correctRate;       // 正确率 (0~1)
    private Integer totalAnswers;     // 作答人数
    private Integer correctCount;     // 正确人数
    private Map<String, Integer> optionDistribution;  // 各选项选择人数
    private List<String> knowledgePoints;              // 关联知识点
}
