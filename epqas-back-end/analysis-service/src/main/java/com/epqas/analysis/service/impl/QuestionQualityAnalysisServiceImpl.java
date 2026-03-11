package com.epqas.analysis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.analysis.entity.QuestionQualityAnalysis;
import com.epqas.analysis.mapper.QuestionQualityAnalysisMapper;
import com.epqas.analysis.mapper.ExamMetricsComputeMapper;
import com.epqas.analysis.service.QuestionQualityAnalysisService;
import com.epqas.analysis.dto.QuestionAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QuestionQualityAnalysisServiceImpl extends
        ServiceImpl<QuestionQualityAnalysisMapper, QuestionQualityAnalysis> implements QuestionQualityAnalysisService {

    @Autowired
    private ExamMetricsComputeMapper computeMapper;

    @Override
    public List<QuestionAnalysisDTO> getQuestionAnalysisDetailsByExamId(Long examId) {
        return computeMapper.getQuestionAnalysisDetailsByExamId(examId);
    }
}
