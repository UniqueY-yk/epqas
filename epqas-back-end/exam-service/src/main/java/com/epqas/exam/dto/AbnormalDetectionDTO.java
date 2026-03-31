package com.epqas.exam.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AbnormalDetectionDTO {
    private Long studentIdA;
    private String studentNameA;
    private Long studentIdB;
    private String studentNameB;
    private int totalQuestions;
    private int identicalWrongCount;
    private double similarityRate;    // identicalWrongCount / totalQuestions
    private List<Map<String, Object>> identicalWrongDetails;  // questionOrder, questionContent, studentChoice, correctAnswer
}
