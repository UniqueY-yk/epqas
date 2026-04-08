package com.epqas.question.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class ExaminationPaperQuestionId implements Serializable {
    private Long paperId; // 试卷ID

    private Long questionId; // 题目ID
}
