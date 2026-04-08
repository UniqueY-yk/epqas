package com.epqas.analysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.analysis.dto.PaperAnalysisVO;
import com.epqas.analysis.entity.ExaminationPaperQualityAnalysis;

import java.util.List;

public interface ExaminationPaperQualityAnalysisService extends IService<ExaminationPaperQualityAnalysis> {

    /**
     * 分页查询试卷分析结果
     * 
     * @param current  当前页
     * @param size     每页数量
     * @param setterId 命题人ID
     * @return 分页试卷分析结果
     */
    Page<PaperAnalysisVO> getPageBySetterId(Integer current, Integer size, Long setterId);

    /**
     * 获取趋势分析数据
     * 
     * @param setterId 命题人ID
     * @param courseId 课程ID
     * @return 趋势分析数据
     */
    List<PaperAnalysisVO> getTrendAnalysis(Long setterId, Long courseId);
}
