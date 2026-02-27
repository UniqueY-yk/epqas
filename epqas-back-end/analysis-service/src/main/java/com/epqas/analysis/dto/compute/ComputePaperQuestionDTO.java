package com.epqas.analysis.dto.compute;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ComputePaperQuestionDTO {
    private Long paperId;
    private Long questionId;
    private BigDecimal scoreValue;
    private Integer questionOrder;
    private Float initialDifficulty;
}
