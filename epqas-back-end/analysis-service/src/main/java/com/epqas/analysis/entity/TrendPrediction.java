package com.epqas.analysis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trend_prediction")
@TableName("trend_prediction")
public class TrendPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long predictionId;

    private Long setterId;

    private Integer courseId;

    private Integer dataPointsUsed;

    private String method;

    private Float predictedDifficulty;
    private Float predictedDiscrimination;
    private Float predictedReliability;
    private Float predictedValidity;

    private Float difficultyLower;
    private Float difficultyUpper;
    private Float discriminationLower;
    private Float discriminationUpper;
    private Float reliabilityLower;
    private Float reliabilityUpper;
    private Float validityLower;
    private Float validityUpper;

    private String difficultyTrend;
    private String discriminationTrend;
    private String reliabilityTrend;
    private String validityTrend;

    @Column(updatable = false, insertable = false)
    private LocalDateTime predictedAt;
}
