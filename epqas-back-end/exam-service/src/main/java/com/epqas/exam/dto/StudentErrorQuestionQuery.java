package com.epqas.exam.dto;

import lombok.Data;

@Data
public class StudentErrorQuestionQuery {
    private Long studentId;
    private Long courseId;
    private Long examId;
    private String questionType;
    
    private Integer current = 1;
    private Integer size = 5;
}
