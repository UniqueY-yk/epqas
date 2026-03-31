package com.epqas.exam.service;

import com.epqas.exam.dto.AbnormalDetectionDTO;
import com.epqas.exam.dto.QuestionStatsDTO;
import com.epqas.exam.dto.KnowledgeMasteryDTO;
import java.util.List;

public interface ClassAnalysisService {
    List<QuestionStatsDTO> getQuestionStats(Long examId);
    List<KnowledgeMasteryDTO> getKnowledgeMastery(Long examId);
    List<AbnormalDetectionDTO> detectAbnormalAnswers(Long examId);
}
