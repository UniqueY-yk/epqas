package com.epqas.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "question")
@TableName("question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long questionId; // 题目ID

    private Integer courseId; // 课程ID

    private Long creatorId; // 创建人ID

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionContent; // 题目内容

    @Column(nullable = false)
    private String questionType; // SingleChoice, MultipleChoice, TrueFalse, FillBlank, ShortAnswer

    @Column(columnDefinition = "JSON")
    private String optionsJson; // 选项JSON

    @Column(columnDefinition = "TEXT")
    private String correctAnswer; // 正确答案

    private Float initialDifficulty; // 初始难度

    @Column(updatable = false)
    private LocalDateTime createdAt; // 创建时间

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
