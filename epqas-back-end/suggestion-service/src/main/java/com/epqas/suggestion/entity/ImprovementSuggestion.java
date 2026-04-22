package com.epqas.suggestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "improvement_suggestion")
@TableName("improvement_suggestion")
public class ImprovementSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long suggestionId; // 建议ID

    private Long paperId; // 试卷ID

    private Long questionId; // 题目ID

    private String suggestionType; // Paper_Structure, Question_Content, Difficulty_Adj

    @Column(nullable = false, columnDefinition = "TEXT")
    private String suggestionText; // 建议文本

    private String generatedRule; // 生成规则

    private Boolean isImplemented; // 是否已实施
}
