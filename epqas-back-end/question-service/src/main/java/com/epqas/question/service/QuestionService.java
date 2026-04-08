package com.epqas.question.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.question.entity.Question;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.question.dto.QuestionDTO;

public interface QuestionService extends IService<Question> {
    /**
     * 创建题目
     * 
     * @param dto 题目DTO
     */
    void createQuestionWithPoints(QuestionDTO dto);

    /**
     * 更新题目
     * 
     * @param dto 题目DTO
     */
    void updateQuestionWithPoints(QuestionDTO dto);

    /**
     * 根据ID获取题目
     * 
     * @param id 题目ID
     * @return 题目DTO
     */
    QuestionDTO getQuestionById(Long id);

    /**
     * 分页获取题目
     * 
     * @param current  当前页
     * @param size     每页数量
     * @param courseId 课程ID
     * @param type     题目类型
     * @param keyword  关键词
     * @return 题目分页数据
     */
    Page<Question> getQuestionPage(Integer current, Integer size, Integer courseId, String type, String keyword);
}
