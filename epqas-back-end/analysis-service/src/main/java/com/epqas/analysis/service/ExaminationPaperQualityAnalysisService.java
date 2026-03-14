package com.epqas.analysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.analysis.dto.PaperAnalysisVO;
import com.epqas.analysis.entity.ExaminationPaperQualityAnalysis;

import java.util.List;

public interface ExaminationPaperQualityAnalysisService extends IService<ExaminationPaperQualityAnalysis> {

    Page<PaperAnalysisVO> getPageBySetterId(Integer current, Integer size, Long setterId);

    List<PaperAnalysisVO> getTrendAnalysisBySetterId(Long setterId);
}
