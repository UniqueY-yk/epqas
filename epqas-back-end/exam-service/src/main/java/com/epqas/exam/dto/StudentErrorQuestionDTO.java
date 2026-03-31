package com.epqas.exam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudentErrorQuestionDTO {
    private Long questionId;
    private Long examId;
    private String examName;
    private LocalDateTime examDate;
    
    private String questionType;
    private String questionContent;
    private String optionsJson;
    private String correctAnswer;
    
    private String studentChoice;
    private BigDecimal scoreObtained;
    private BigDecimal maxScore;
    
    private List<String> knowledgePoints;
}
