package com.epqas.analysis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "question_quality_analysis")
@TableName("question_quality_analysis")
public class QuestionQualityAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long qAnalysisId;

    private Long examId;

    private Long questionId;

    private Float correctResponseRate;

    private Float discriminationIndex;

    @Column(columnDefinition = "JSON")
    private String selectionDistributionJson;

    private Boolean isTooEasy;

    private Boolean isLowDiscrimination;

    private String diagnosisTag;
}
