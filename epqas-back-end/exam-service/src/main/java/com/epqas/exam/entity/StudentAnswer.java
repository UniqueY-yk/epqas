package com.epqas.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.epqas.common.handler.EncryptConverter;
import com.epqas.common.handler.EncryptTypeHandler;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "student_answer")
@TableName(value = "student_answer", autoResultMap = true)
public class StudentAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long answerId; // 答案ID

    private Long resultId; // 考试结果ID

    private Long questionId; // 题目ID

    @Convert(converter = EncryptConverter.class)
    @TableField(typeHandler = EncryptTypeHandler.class)
    @Column(columnDefinition = "TEXT")
    private String studentChoice; // 学生答案（AES加密存储）

    private BigDecimal scoreObtained; // 获得分数

    private Boolean isCorrect; // 是否正确
}
