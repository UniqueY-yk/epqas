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
    private Long paperId;

    @Column(nullable = false)
    private String title;

    private Integer courseId;

    private Long setterId;

    private Integer totalScore;

    private Integer durationMinutes;

    private Float targetDifficulty;

    private String status; // Draft, Published, Archived

    @Column(updatable = false)
    private LocalDateTime createdAt;

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
