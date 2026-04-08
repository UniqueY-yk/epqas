package com.epqas.analysis.service;

public interface AnalysisComputationService {

    /**
     * 计算整个考试及其相关问题的心理测量指标
     * 
     * @param examId 考试实例ID
     */
    void calculateExamIndicators(Long examId);
}
