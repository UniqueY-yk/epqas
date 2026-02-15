package com.epqas.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "examination_paper_questions")
@TableName("examination_paper_questions")
@IdClass(ExaminationPaperQuestionId.class)
public class ExaminationPaperQuestion {
    @Id
    private Long paperId;

    @Id
    private Long questionId;

    private BigDecimal scoreValue;

    private Integer questionOrder;
}
