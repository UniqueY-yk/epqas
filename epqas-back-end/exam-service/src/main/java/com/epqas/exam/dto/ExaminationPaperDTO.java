package com.epqas.exam.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExaminationPaperDTO {
    private Long paperId; // 试卷ID

    private String title; // 试卷标题

    private Integer courseId; // 课程ID

    private Long setterId; // 命题人ID

    private String setterName; // 命题人姓名

    private Integer totalScore; // 总分

    private Integer durationMinutes; // 考试时长

    private Float targetDifficulty; // 目标难度

    private String status; // 试卷状态

    private LocalDateTime createdAt; // 创建时间

    private LocalDateTime updatedAt; // 更新时间

    private List<PaperQuestionDTO> questions; // 试卷问题列表
}
