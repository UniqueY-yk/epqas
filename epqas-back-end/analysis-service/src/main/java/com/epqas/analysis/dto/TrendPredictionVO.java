package com.epqas.analysis.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrendPredictionVO {

    private Long predictionId;

    private Long setterId;
    private Integer courseId;

    private String method;               // "WMA" or "OLS"
    private Integer dataPointsUsed;      // number of historical data points used

    private Float predictedDifficulty;
    private Float predictedDiscrimination;
    private Float predictedReliability;
    private Float predictedValidity;

    // Confidence intervals (±1σ)
    private Float difficultyLower;
    private Float difficultyUpper;
    private Float discriminationLower;
    private Float discriminationUpper;
    private Float reliabilityLower;
    private Float reliabilityUpper;
    private Float validityLower;
    private Float validityUpper;

    // Trend direction text descriptions
    private String difficultyTrend;
    private String discriminationTrend;
    private String reliabilityTrend;
    private String validityTrend;

    private LocalDateTime predictedAt;
}
