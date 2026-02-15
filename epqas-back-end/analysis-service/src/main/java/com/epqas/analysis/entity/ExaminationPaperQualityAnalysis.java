package com.epqas.analysis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "examination_paper_quality_analysis")
@TableName("examination_paper_quality_analysis")
public class ExaminationPaperQualityAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long analysisId;

    @Column(unique = true)
    private Long examId;

    private BigDecimal averageScore;

    private BigDecimal stdDeviation;

    private BigDecimal highestScore;

    private BigDecimal lowestScore;

    private Float reliabilityCoefficient;

    private Float validityCoefficient;

    private Float knowledgeCoverageRate;

    private Float overallDifficulty;

    private Float overallDiscrimination;

    private Boolean isAbnormal;

    @Column(updatable = false)
    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }
}
