package com.epqas.exam.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class ExaminationPaperQuestionId implements Serializable {
    private Long paperId;
    private Long questionId;
}
