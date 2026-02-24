package com.epqas.question.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.question.entity.Question;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.question.dto.QuestionDTO;

public interface QuestionService extends IService<Question> {
    void createQuestionWithPoints(QuestionDTO dto);

    void updateQuestionWithPoints(QuestionDTO dto);

    QuestionDTO getQuestionById(Long id);

    Page<Question> getQuestionPage(Integer current, Integer size, Integer courseId, String type, String keyword);
}
