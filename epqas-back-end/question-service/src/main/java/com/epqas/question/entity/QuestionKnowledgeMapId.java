package com.epqas.question.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class QuestionKnowledgeMapId implements Serializable {
    private Long questionId; // 题目ID

    private Integer pointId; // 知识点ID
}
