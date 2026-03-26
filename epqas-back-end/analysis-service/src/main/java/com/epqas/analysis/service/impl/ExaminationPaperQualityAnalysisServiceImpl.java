package com.epqas.analysis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.analysis.entity.ExaminationPaperQualityAnalysis;
import com.epqas.analysis.mapper.ExaminationPaperQualityAnalysisMapper;
import com.epqas.analysis.service.ExaminationPaperQualityAnalysisService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.analysis.dto.PaperAnalysisVO;

import java.util.List;

@Service
public class ExaminationPaperQualityAnalysisServiceImpl
        extends ServiceImpl<ExaminationPaperQualityAnalysisMapper, ExaminationPaperQualityAnalysis>
        implements ExaminationPaperQualityAnalysisService {

    @Override
    public Page<PaperAnalysisVO> getPageBySetterId(Integer current, Integer size, Long setterId) {
        Page<PaperAnalysisVO> page = new Page<>(current, size);
        return baseMapper.selectPageBySetterId(page, setterId);
    }

    @Override
    public List<PaperAnalysisVO> getTrendAnalysis(Long setterId, Long courseId) {
        return baseMapper.selectTrendBySetterId(setterId, courseId);
    }
}
