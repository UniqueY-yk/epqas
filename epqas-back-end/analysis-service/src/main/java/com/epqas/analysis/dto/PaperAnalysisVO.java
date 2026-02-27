package com.epqas.analysis.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaperAnalysisVO {
    private Long analysisId;
    private Long examId;
    private Long paperId;
    private String paperTitle;
    private String courseName;
    private LocalDateTime examDate;

    private BigDecimal averageScore;
    private BigDecimal highestScore;
    private BigDecimal lowestScore;

    private Float reliabilityCoefficient;
    private Float validityCoefficient;
    private Float knowledgeCoverageRate;
    private Float overallDifficulty;
    private Float overallDiscrimination;
    private Boolean isAbnormal;
}
