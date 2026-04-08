package com.epqas.question.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "question_knowledge_map")
@TableName("question_knowledge_map")
@IdClass(QuestionKnowledgeMapId.class)
public class QuestionKnowledgeMap {
    @Id
    private Long questionId; // 题目ID

    @Id
    private Integer pointId; // 知识点ID
}
