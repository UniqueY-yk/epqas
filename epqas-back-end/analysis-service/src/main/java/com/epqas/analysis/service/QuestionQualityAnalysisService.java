package com.epqas.analysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.analysis.entity.QuestionQualityAnalysis;
import com.epqas.analysis.dto.QuestionAnalysisDTO;
import java.util.List;

public interface QuestionQualityAnalysisService extends IService<QuestionQualityAnalysis> {

    /**
     * Retrieves detailed question-level quality analysis combined with question
     * text.
     */
    List<QuestionAnalysisDTO> getQuestionAnalysisDetailsByExamId(Long examId);
}
