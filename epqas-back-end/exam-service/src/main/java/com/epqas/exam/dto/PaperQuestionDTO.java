package com.epqas.exam.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaperQuestionDTO {
    private Long questionId;
    private BigDecimal scoreValue;
    private Integer questionOrder;
    private String questionContent;
    private String questionType;
}
