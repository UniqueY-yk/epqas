package com.epqas.analysis.service;

public interface AnalysisComputationService {

    /**
     * 计算整个试卷及其相关问题的心理测量指标
     * 
     * @param paperId 试卷ID
     */
    void calculatePaperIndicators(Long paperId);
}
