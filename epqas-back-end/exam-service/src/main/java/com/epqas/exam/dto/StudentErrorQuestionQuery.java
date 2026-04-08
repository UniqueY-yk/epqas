package com.epqas.exam.dto;

import lombok.Data;

@Data
public class StudentErrorQuestionQuery {
    private Long studentId; // 学生ID
    
    private Long courseId; // 课程ID
    
    private Long examId; // 考试ID
    
    private String questionType; // 题目类型
    
    private Integer current = 1; // 当前页
    
    private Integer size = 5; // 每页数量
}
