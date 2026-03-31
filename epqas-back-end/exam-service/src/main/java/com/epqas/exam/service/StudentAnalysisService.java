package com.epqas.exam.service;

import com.epqas.exam.dto.KnowledgeMasteryDTO;
import java.util.List;

public interface StudentAnalysisService {
    /**
     * Get a student's personal knowledge point mastery based on their past exam performance.
     * @param studentId The student's user ID
     * @return List of KnowledgeMasteryDTO sorted from weakest to strongest
     */
    List<KnowledgeMasteryDTO> getStudentKnowledgeMastery(Long studentId);
}
