package com.epqas.analysis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epqas.analysis.entity.ExaminationPaperQualityAnalysis;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.analysis.dto.PaperAnalysisVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExaminationPaperQualityAnalysisMapper extends BaseMapper<ExaminationPaperQualityAnalysis> {

    /**
     * 分页查询试卷分析
     * 
     * @param page     分页参数
     * @param setterId 命题人ID
     * @return 分页查询结果
     */
    Page<PaperAnalysisVO> selectPageBySetterId(@Param("page") Page<PaperAnalysisVO> page,
            @Param("setterId") Long setterId);

    /**
     * 查询试卷分析趋势
     * 
     * @param setterId 命题人ID
     * @param courseId 课程ID
     * @return 试卷分析趋势
     */
    List<PaperAnalysisVO> selectTrendBySetterId(@Param("setterId") Long setterId, @Param("courseId") Long courseId);
}
