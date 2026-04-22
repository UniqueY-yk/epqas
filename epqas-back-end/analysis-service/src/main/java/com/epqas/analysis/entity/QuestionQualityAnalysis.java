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
    private Long qAnalysisId; // 题目分析ID

    private Long paperId; // 试卷ID

    private Long questionId; // 题目ID

    private Float correctResponseRate; // 正确反应率

    private Float difficultyIndex; // 难度系数P (极端组法)

    private Float discriminationIndex; // 区分度

    private Float validityIndex; // 效度 (Pearson r)

    @Column(columnDefinition = "JSON")
    private String selectionDistributionJson; // 选项分布

    private Boolean isTooEasy; // 是否太简单

    private Boolean isLowDiscrimination; // 是否区分度低

    private String diagnosisTag; // 诊断标签

    private String difficultyEvaluation; // 难度定性评价

    private String discriminationEvaluation; // 区分度定性评价
}
