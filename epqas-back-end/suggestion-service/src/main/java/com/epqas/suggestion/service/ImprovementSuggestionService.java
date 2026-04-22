package com.epqas.suggestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.suggestion.entity.ImprovementSuggestion;

import java.util.List;

public interface ImprovementSuggestionService extends IService<ImprovementSuggestion> {
    List<ImprovementSuggestion> getSuggestionsByPaperAndQuestion(Long paperId, Long questionId);
}
