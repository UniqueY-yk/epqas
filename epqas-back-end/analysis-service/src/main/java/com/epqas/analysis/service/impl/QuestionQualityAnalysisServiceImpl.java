package com.epqas.analysis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.analysis.entity.QuestionQualityAnalysis;
import com.epqas.analysis.mapper.QuestionQualityAnalysisMapper;
import com.epqas.analysis.mapper.ExamMetricsComputeMapper;
import com.epqas.analysis.service.QuestionQualityAnalysisService;
import com.epqas.analysis.dto.QuestionAnalysisDTO;
import com.epqas.common.utils.AESUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionQualityAnalysisServiceImpl extends
        ServiceImpl<QuestionQualityAnalysisMapper, QuestionQualityAnalysis> implements QuestionQualityAnalysisService {

    @Autowired
    private ExamMetricsComputeMapper computeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

            // 解密 selectionDistributionJson 的键
            String jsonStr = dto.getSelectionDistributionJson();
            if (jsonStr != null && !jsonStr.trim().isEmpty()) {
                try {
                    Map<String, Object> rawMap = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
                    Map<String, Object> decryptedMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                        String rawKey = entry.getKey();
                        try {
                            String decryptedKey = AESUtil.decrypt(rawKey);
                            decryptedMap.put(decryptedKey, entry.getValue());
                        } catch (Exception e) {
                            // 解密失败则保留原值
                            decryptedMap.put(rawKey, entry.getValue());
                        }
                    }
                    dto.setSelectionDistributionJson(objectMapper.writeValueAsString(decryptedMap));
                } catch (Exception e) {
                    // JSON 解析或序列化失败时，忽略并保留原字符串
                }
            }
        }

        return dtos;
    }
}
