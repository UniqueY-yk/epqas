package com.epqas.exam.service;

import com.epqas.exam.dto.KnowledgeMasteryDTO;
import java.util.List;

public interface StudentAnalysisService {
    /**
     * 获取学生知识点掌握度
     * @param studentId 学生ID
     * @return 知识点掌握度列表
     */
    List<KnowledgeMasteryDTO> getStudentKnowledgeMastery(Long studentId);

    /**
     * 获取学生错题
     * @param query 错题查询条件
     * @return 错题列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.epqas.exam.dto.StudentErrorQuestionDTO> getStudentErrorQuestions(com.epqas.exam.dto.StudentErrorQuestionQuery query);
}

