package com.epqas.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.question.dto.QuestionDTO;
import com.epqas.question.entity.Question;
import com.epqas.question.entity.QuestionKnowledgeMap;
import com.epqas.question.mapper.QuestionMapper;
import com.epqas.question.service.QuestionKnowledgeMapService;
import com.epqas.question.service.QuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private QuestionKnowledgeMapService questionKnowledgeMapService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createQuestionWithPoints(QuestionDTO dto) {
        Question question = new Question();
        BeanUtils.copyProperties(dto, question);
        this.save(question); // Gets generated ID

        if (dto.getPointIds() != null && !dto.getPointIds().isEmpty()) {
            List<QuestionKnowledgeMap> maps = new ArrayList<>();
            for (Integer pointId : dto.getPointIds()) {
                QuestionKnowledgeMap map = new QuestionKnowledgeMap();
                map.setQuestionId(question.getQuestionId());
                map.setPointId(pointId);
                maps.add(map);
            }
            questionKnowledgeMapService.saveBatch(maps);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestionWithPoints(QuestionDTO dto) {
        Question question = new Question();
        BeanUtils.copyProperties(dto, question);
        this.updateById(question);

        // Delete old mappings
        QueryWrapper<QuestionKnowledgeMap> wrapper = new QueryWrapper<>();
        wrapper.eq("question_id", question.getQuestionId());
        questionKnowledgeMapService.remove(wrapper);

        // Insert new mappings
        if (dto.getPointIds() != null && !dto.getPointIds().isEmpty()) {
            List<QuestionKnowledgeMap> maps = new ArrayList<>();
            for (Integer pointId : dto.getPointIds()) {
                QuestionKnowledgeMap map = new QuestionKnowledgeMap();
                map.setQuestionId(question.getQuestionId());
                map.setPointId(pointId);
                maps.add(map);
            }
            questionKnowledgeMapService.saveBatch(maps);
        }
    }

    @Override
    public QuestionDTO getQuestionById(Long id) {
        Question question = this.getById(id);
        if (question == null)
            return null;

        QuestionDTO dto = new QuestionDTO();
        BeanUtils.copyProperties(question, dto);

        QueryWrapper<QuestionKnowledgeMap> wrapper = new QueryWrapper<>();
        wrapper.eq("question_id", id);
        List<QuestionKnowledgeMap> maps = questionKnowledgeMapService.list(wrapper);
        List<Integer> pointIds = maps.stream().map(QuestionKnowledgeMap::getPointId).collect(Collectors.toList());
        dto.setPointIds(pointIds);

        return dto;
    }

    @Override
    public Page<Question> getQuestionPage(Integer current, Integer size, Integer courseId, String type,
            String keyword) {
        Page<Question> page = new Page<>(current, size);
        QueryWrapper<Question> wrapper = new QueryWrapper<>();

        if (courseId != null) {
            wrapper.eq("course_id", courseId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("question_type", type);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("question_content", keyword);
        }

        wrapper.orderByDesc("created_at");
        return this.page(page, wrapper);
    }
}
