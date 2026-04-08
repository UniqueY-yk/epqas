package com.epqas.exam.dto;

import lombok.Data;

@Data
public class KnowledgeMasteryDTO {
    private Long pointId; // 知识点ID

    private String pointName; // 知识点名称

    private Integer totalQuestions; // 该知识点涉及的题目数

    private Integer totalAnswers; // 总作答次数

    private Integer correctCount; // 正确次数

    private Double masteryRate; // 掌握率 (0~1)

    private String level; // "excellent" / "good" / "weak"
}
