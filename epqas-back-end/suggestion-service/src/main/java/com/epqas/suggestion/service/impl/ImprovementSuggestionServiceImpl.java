package com.epqas.suggestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.suggestion.entity.ImprovementSuggestion;
import com.epqas.suggestion.mapper.ImprovementSuggestionMapper;
import com.epqas.suggestion.service.ImprovementSuggestionService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;

@Service
public class ImprovementSuggestionServiceImpl extends ServiceImpl<ImprovementSuggestionMapper, ImprovementSuggestion> implements ImprovementSuggestionService {
    
    @Override
    public List<ImprovementSuggestion> getSuggestionsByExamAndQuestion(Long examId, Long questionId) {
        LambdaQueryWrapper<ImprovementSuggestion> wrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            wrapper.eq(ImprovementSuggestion::getExamId, examId);
        }
        if (questionId != null) {
            wrapper.eq(ImprovementSuggestion::getQuestionId, questionId);
        }
        return this.list(wrapper);
    }
}
