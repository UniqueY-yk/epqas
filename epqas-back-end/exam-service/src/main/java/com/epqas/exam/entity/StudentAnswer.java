package com.epqas.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "student_answer")
@TableName("student_answer")
public class StudentAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long answerId;

    private Long resultId;

    private Long questionId;

    @Column(columnDefinition = "TEXT")
    private String studentChoice;

    private BigDecimal scoreObtained;

    private Boolean isCorrect;
}
