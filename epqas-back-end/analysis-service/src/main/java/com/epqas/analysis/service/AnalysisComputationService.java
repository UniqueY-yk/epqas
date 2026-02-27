package com.epqas.analysis.service;

public interface AnalysisComputationService {

    /**
     * Compute psychometric indicators for an entire examination and its
     * corresponding
     * questions based on the submitted student answers.
     * 
     * @param examId The ID of the exam instance
     */
    void calculateExamIndicators(Long examId);
}
