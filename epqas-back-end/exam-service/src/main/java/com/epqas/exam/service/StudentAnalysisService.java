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

    /**
     * Get a student's error questions from past exams for targeted revision.
     * @param query The filtering and pagination constraints
     * @return Page of ErrorQuestions chronologically sorted
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.epqas.exam.dto.StudentErrorQuestionDTO> getStudentErrorQuestions(com.epqas.exam.dto.StudentErrorQuestionQuery query);
}

