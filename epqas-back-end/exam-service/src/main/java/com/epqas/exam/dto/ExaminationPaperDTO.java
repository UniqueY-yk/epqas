package com.epqas.exam.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExaminationPaperDTO {
    private Long paperId;
    private String title;
    private Integer courseId;
    private Long setterId;
    private String setterName;
    private Integer totalScore;
    private Integer durationMinutes;
    private Float targetDifficulty;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PaperQuestionDTO> questions;
}
