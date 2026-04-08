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
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class QuestionQualityAnalysisServiceImpl extends
        ServiceImpl<QuestionQualityAnalysisMapper, QuestionQualityAnalysis> implements QuestionQualityAnalysisService {

    @Autowired
    private ExamMetricsComputeMapper computeMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 根据考试ID获取题目分析详情
     * 
     * @param examId 考试ID
     * @return 题目分析详情列表
     */
    @Override
    public List<QuestionAnalysisDTO> getQuestionAnalysisDetailsByExamId(Long examId) {
        List<QuestionAnalysisDTO> dtos = computeMapper.getQuestionAnalysisDetailsByExamId(examId);

        for (QuestionAnalysisDTO dto : dtos) {
            List<String> suggestions = jdbcTemplate.queryForList(
                    "SELECT suggestion_text FROM improvement_suggestion WHERE exam_id = ? AND question_id = ?",
                    String.class,
                    examId,
                    dto.getQuestionId());
            dto.setSuggestions(suggestions);
        }

        return dtos;
    }
}
