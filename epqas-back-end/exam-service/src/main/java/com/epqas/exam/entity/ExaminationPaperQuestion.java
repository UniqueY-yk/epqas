package com.epqas.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "examination_paper_question")
@TableName("examination_paper_question")
@IdClass(ExaminationPaperQuestion.ExaminationPaperQuestionId.class)
public class ExaminationPaperQuestion {

    @Id
    private Long paperId;

    @Id
    private Long questionId;

    private BigDecimal scoreValue;

    private Integer questionOrder;

    @Data
    public static class ExaminationPaperQuestionId implements Serializable {
        private Long paperId;
        private Long questionId;
    }
}
