package com.epqas.suggestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.suggestion.entity.ImprovementSuggestion;
import com.epqas.suggestion.mapper.ImprovementSuggestionMapper;
import com.epqas.suggestion.service.ImprovementSuggestionService;
import org.springframework.stereotype.Service;

@Service
public class ImprovementSuggestionServiceImpl extends ServiceImpl<ImprovementSuggestionMapper, ImprovementSuggestion> implements ImprovementSuggestionService {
}
