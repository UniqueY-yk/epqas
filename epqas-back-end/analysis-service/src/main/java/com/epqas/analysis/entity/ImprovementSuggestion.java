package com.epqas.analysis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "improvement_suggestions")
@TableName("improvement_suggestions")
public class ImprovementSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long suggestionId;

    private Long examId;

    private Long questionId;

    private String suggestionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String suggestionText;

    private String generatedRule;

    private Boolean isImplemented;
}
