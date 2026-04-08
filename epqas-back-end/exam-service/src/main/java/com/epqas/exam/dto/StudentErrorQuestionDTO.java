package com.epqas.exam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudentErrorQuestionDTO {
    private Long questionId; // 题目ID
    
    private Long examId; // 考试ID
    
    private String examName; // 考试名称
    
    private LocalDateTime examDate; // 考试日期
    
    private String questionType; // 题目类型
    
    private String questionContent; // 题目内容
    
    private String optionsJson; // 选项JSON
    
    private String correctAnswer; // 正确答案
    
    private String studentChoice; // 学生选择
    
    private BigDecimal scoreObtained; // 获得分数
    
    private BigDecimal maxScore; // 最大分数
    
    private List<String> knowledgePoints; // 知识点列表
}
