package com.epqas.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "student_exam_result")
@TableName("student_exam_result")
public class StudentExamResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long resultId;

    private Long examId;

    private Long studentId;

    private BigDecimal totalScore;

    private Boolean isAbsent;

    private LocalDateTime submittedAt;
}
