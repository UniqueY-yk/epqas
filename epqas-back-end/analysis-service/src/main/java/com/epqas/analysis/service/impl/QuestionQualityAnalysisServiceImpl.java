package com.epqas.analysis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.analysis.entity.QuestionQualityAnalysis;
import com.epqas.analysis.mapper.QuestionQualityAnalysisMapper;
import com.epqas.analysis.service.QuestionQualityAnalysisService;
import org.springframework.stereotype.Service;

@Service
public class QuestionQualityAnalysisServiceImpl extends ServiceImpl<QuestionQualityAnalysisMapper, QuestionQualityAnalysis> implements QuestionQualityAnalysisService {
}
