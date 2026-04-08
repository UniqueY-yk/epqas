package com.epqas.question.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long questionId; // 题目ID

    private Integer courseId; // 课程ID

    private Long creatorId; // 创建人ID

    private String questionContent; // 题目内容

    private String questionType; // 题目类型

    private String optionsJson; // 选项JSON

    private String correctAnswer; // 正确答案

    private Float initialDifficulty; // 初始难度

    private List<Integer> pointIds; // 知识点ID列表
}
