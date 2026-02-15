package com.epqas.core.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class QuestionKnowledgeMapId implements Serializable {
    private Long questionId;
    private Integer pointId;
}
