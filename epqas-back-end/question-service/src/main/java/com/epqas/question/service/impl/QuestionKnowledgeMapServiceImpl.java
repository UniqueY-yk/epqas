package com.epqas.question.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.question.entity.QuestionKnowledgeMap;
import com.epqas.question.mapper.QuestionKnowledgeMapMapper;
import com.epqas.question.service.QuestionKnowledgeMapService;
import org.springframework.stereotype.Service;

@Service
public class QuestionKnowledgeMapServiceImpl extends ServiceImpl<QuestionKnowledgeMapMapper, QuestionKnowledgeMap>
        implements QuestionKnowledgeMapService {
}
