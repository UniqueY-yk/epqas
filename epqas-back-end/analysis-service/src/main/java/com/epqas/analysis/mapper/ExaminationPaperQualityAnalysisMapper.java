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

    Page<PaperAnalysisVO> selectPageBySetterId(@Param("page") Page<PaperAnalysisVO> page,
            @Param("setterId") Long setterId);

    List<PaperAnalysisVO> selectTrendBySetterId(@Param("setterId") Long setterId);
}
