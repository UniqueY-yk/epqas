package com.epqas.analysis.dto;

import lombok.Data;

@Data
public class QuestionAnalysisDTO {
    private Long questionId; // 题目ID

    private String stem; // 题目文本

    private String questionType; // 题目类型

    private Float correctResponseRate; // 正确反应率 (难度)

    private Float difficultyIndex; // 难度系数P (极端组法)

    private Float discriminationIndex; // 区分度

    private Float validityIndex; // 效度 (Pearson r)

    private Boolean isTooEasy; // 是否太简单

    private Boolean isLowDiscrimination; // 是否区分度低

    private String diagnosisTag; // 诊断标签

    private Boolean isAbnormal; // 是否异常

    private String selectionDistributionJson; // 选项分布JSON

    private String optionsJson; // 选项JSON

    private String correctAnswer; // 正确答案

    private String difficultyEvaluation; // 难度评价

    private String discriminationEvaluation; // 区分度评价

    private java.util.List<String> suggestions; // 建议列表
}
