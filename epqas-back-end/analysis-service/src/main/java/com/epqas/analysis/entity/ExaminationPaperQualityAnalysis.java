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
    private Long analysisId; // 分析ID

    private Long paperId; // 试卷ID

    private BigDecimal averageScore; // 平均分

    private BigDecimal stdDeviation; // 标准差

    private BigDecimal highestScore; // 最高分

    private BigDecimal lowestScore; // 最低分

    private Float reliabilityCoefficient; // 信度系数

    private Float validityCoefficient; // 效度系数

    private Float knowledgeCoverageRate; // 知识覆盖率

    private Float overallDifficulty; // 整体难度

    private Float overallDiscrimination; // 整体区分度

    private Boolean isAbnormal; // 是否异常

    private Float skewness; // 偏度

    private Float kurtosis; // 峰度

    private String reliabilityEvaluation; // 信度定性评价

    private String difficultyEvaluation; // 难度定性评价

    private String discriminationEvaluation; // 区分度定性评价

    @Column(updatable = false)
    private LocalDateTime calculatedAt; // 计算时间

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }
}
