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
    private Long paperId; // 试卷ID

    @Id
    private Long questionId; // 题目ID

    private BigDecimal scoreValue; // 分值

    private Integer questionOrder; // 题目顺序

    @Data
    public static class ExaminationPaperQuestionId implements Serializable {
        private Long paperId;
        private Long questionId;
    }
}
