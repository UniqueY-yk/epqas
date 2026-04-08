package com.epqas.question.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.question.entity.ExaminationPaperQuestion;
import com.epqas.question.mapper.ExaminationPaperQuestionMapper;
import com.epqas.question.service.ExaminationPaperQuestionService;
import org.springframework.stereotype.Service;

@Service
public class ExaminationPaperQuestionServiceImpl
        extends ServiceImpl<ExaminationPaperQuestionMapper, ExaminationPaperQuestion>
        implements ExaminationPaperQuestionService {
}
