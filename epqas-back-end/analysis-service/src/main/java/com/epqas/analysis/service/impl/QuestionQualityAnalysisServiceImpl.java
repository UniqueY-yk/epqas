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

    /**
     * 根据试卷ID获取题目分析详情
     * 
     * @param paperId 试卷ID
     * @return 题目分析详情列表
     */
    @Override
    public List<QuestionAnalysisDTO> getQuestionAnalysisDetailsByPaperId(Long paperId) {
        List<QuestionAnalysisDTO> dtos = computeMapper.getQuestionAnalysisDetailsByPaperId(paperId);

        for (QuestionAnalysisDTO dto : dtos) {
            List<String> suggestions = computeMapper.getSuggestionTextsByPaperIdAndQuestionId(paperId, dto.getQuestionId());
            dto.setSuggestions(suggestions);
        }

        return dtos;
    }
}
