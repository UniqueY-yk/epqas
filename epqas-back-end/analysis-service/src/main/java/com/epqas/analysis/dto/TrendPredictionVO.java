package com.epqas.analysis.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrendPredictionVO {

    private Long predictionId;

    private Long setterId;
    private Integer courseId;

    private String method;               // 方法："加权移动平均(WMA)" 或 "线性回归(OLS)"
    private Integer dataPointsUsed;      // 使用的历史数据点数量

    private Float predictedDifficulty;
    private Float predictedDiscrimination;
    private Float predictedReliability;
    private Float predictedValidity;

    // 置信区间（±1σ）
    private Float difficultyLower;
    private Float difficultyUpper;
    private Float discriminationLower;
    private Float discriminationUpper;
    private Float reliabilityLower;
    private Float reliabilityUpper;
    private Float validityLower;
    private Float validityUpper;

    // 趋势方向文本描述
    private String difficultyTrend;
    private String discriminationTrend;
    private String reliabilityTrend;
    private String validityTrend;

    private LocalDateTime predictedAt;
}
