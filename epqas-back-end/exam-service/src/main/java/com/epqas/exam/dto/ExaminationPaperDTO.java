package com.epqas.exam.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExaminationPaperDTO {
    private Long paperId;
    private String title;
    private Integer courseId;
    private Long setterId;
    private Integer totalScore;
    private Integer durationMinutes;
    private Float targetDifficulty;
    private String status;

    private List<PaperQuestionDTO> questions;
}
