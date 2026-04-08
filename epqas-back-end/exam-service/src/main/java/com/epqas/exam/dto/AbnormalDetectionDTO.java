package com.epqas.exam.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AbnormalDetectionDTO {
    private Long studentIdA; // 学生A的ID

    private String studentNameA; // 学生A的名字

    private Long studentIdB; // 学生B的ID

    private String studentNameB; // 学生B的名字

    private int totalQuestions; // 总题目数

    private int identicalWrongCount; // 相同错误数

    private double similarityRate; // 相同错误率

    private List<Map<String, Object>> identicalWrongDetails; // 相同错误详情
}
