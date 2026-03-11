package com.epqas.analysis.dto;

import lombok.Data;

@Data
public class QuestionAnalysisDTO {
    private Long questionId;
    private String stem;
    private String questionType;
    private Float correctResponseRate; // Difficulty
    private Float discriminationIndex; // Discrimination
    private Boolean isTooEasy;
    private Boolean isLowDiscrimination;
    private String diagnosisTag;
    private Boolean isAbnormal;
}
