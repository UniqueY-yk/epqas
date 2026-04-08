package com.epqas.exam.service;

import com.epqas.exam.dto.AbnormalDetectionDTO;
import com.epqas.exam.dto.QuestionStatsDTO;
import com.epqas.exam.dto.KnowledgeMasteryDTO;
import java.util.List;

public interface ClassAnalysisService {
    /**
     * 获取题目统计数据
     * @param examId 考试ID
     * @return 题目统计数据
     */
    List<QuestionStatsDTO> getQuestionStats(Long examId);
    /**
     * 获取知识点掌握度
     * @param examId 考试ID
     * @return 知识点掌握度
     */
    List<KnowledgeMasteryDTO> getKnowledgeMastery(Long examId);
    /**
     * 检测异常答案
     * @param examId 考试ID
     * @return 异常答案列表
     */
    List<AbnormalDetectionDTO> detectAbnormalAnswers(Long examId);
}
