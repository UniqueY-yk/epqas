package com.epqas.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "examination_paper")
@TableName("examination_paper")
public class ExaminationPaper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long paperId; // 试卷ID

    @Column(nullable = false)
    private String title; // 试卷标题

    private Integer courseId; // 课程ID

    private Long setterId; // 命题人ID

    private Integer totalScore; // 总分

    private Integer durationMinutes; // 考试时长

    private Float targetDifficulty; // 目标难度

    private String status; // Draft, Published, Archived

    @Column(updatable = false)
    private LocalDateTime createdAt; // 创建时间

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "Draft";
        }
        if (totalScore == null) {
            totalScore = 100;
        }
    }
}
