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

    /**
     * 分页查询试卷分析结果
     * 
     * @param current  当前页
     * @param size     每页数量
     * @param setterId 命题人ID
     * @param courseId 课程ID
     * @param paperTitle 试卷名称
     * @return 分页试卷分析结果
     */
    @Override
    public Page<PaperAnalysisVO> getPageBySetterId(Integer current, Integer size, Long setterId, Long courseId, String paperTitle) {
        Page<PaperAnalysisVO> page = new Page<>(current, size);
        return baseMapper.selectPageBySetterId(page, setterId, courseId, paperTitle);
    }

    /**
     * 获取趋势分析数据
     * 
     * @param setterId 命题人ID
     * @param courseId 课程ID
     * @return 趋势分析数据
     */
    @Override
    public List<PaperAnalysisVO> getTrendAnalysis(Long setterId, Long courseId) {
        return baseMapper.selectTrendBySetterId(setterId, courseId);
    }
}
