package com.epqas.analysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.analysis.entity.QuestionQualityAnalysis;
import com.epqas.analysis.dto.QuestionAnalysisDTO;
import java.util.List;

public interface QuestionQualityAnalysisService extends IService<QuestionQualityAnalysis> {

    /**
     * 获取包含题目文本的详细题目级质量分析。
     * 
     * @param examId 考试ID
     * @return 题目分析详情列表
     */
    List<QuestionAnalysisDTO> getQuestionAnalysisDetailsByExamId(Long examId);
}
