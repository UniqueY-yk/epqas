package com.epqas.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "questions")
@TableName("questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long questionId;

    private Integer courseId;

    private Long creatorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionContent;

    @Column(nullable = false)
    private String questionType; // MCQ, TrueFalse, FillBlank, Essay

    @Column(columnDefinition = "JSON")
    private String optionsJson;

    @Column(columnDefinition = "TEXT")
    private String correctAnswer;

    private Float initialDifficulty;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
