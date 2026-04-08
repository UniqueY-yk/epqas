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
    private Long resultId; // 考试结果ID

    private Long examId; // 考试ID

    private Long studentId; // 学生ID

    private BigDecimal totalScore; // 总分

    private Boolean isAbsent; // 是否缺考

    private LocalDateTime submittedAt; // 提交时间
}
