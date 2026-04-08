package com.epqas.question.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "examination_paper_question")
@TableName("examination_paper_question")
@IdClass(ExaminationPaperQuestionId.class)
public class ExaminationPaperQuestion {
    @Id
    private Long paperId; // 试卷ID

    @Id
    private Long questionId; // 题目ID

    private BigDecimal scoreValue; // 分值

    private Integer questionOrder; // 题目顺序
}
