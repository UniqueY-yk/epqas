package com.epqas.exam.entity;

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

    private String title;

    private Integer courseId;

    private Long setterId;

    private Integer totalScore;

    private Integer durationMinutes;

    private Float targetDifficulty;

    private String status;

    private LocalDateTime createdAt;
}
