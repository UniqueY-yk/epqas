package com.epqas.analysis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epqas.analysis.entity.ExaminationPaperQualityAnalysis;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.analysis.dto.PaperAnalysisVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 试卷质量分析Mapper
 */
@Mapper
public interface ExaminationPaperQualityAnalysisMapper extends BaseMapper<ExaminationPaperQualityAnalysis> {

    /**
     * 分页查询试卷分析
     * 
     * @param page     分页参数
     * @param setterId 命题人ID
     * @return 分页查询结果
     */
    @Select("<script>" +
            "SELECT " +
            "    eqa.analysis_id AS analysisId, " +
            "    eqa.exam_id AS examId, " +
            "    ep.paper_id AS paperId, " +
            "    ep.title AS paperTitle, " +
            "    c.course_name AS courseName, " +
            "    e.exam_date AS examDate, " +
            "    eqa.average_score AS averageScore, " +
            "    eqa.std_deviation AS stdDeviation, " +
            "    eqa.highest_score AS highestScore, " +
            "    eqa.lowest_score AS lowestScore, " +
            "    eqa.reliability_coefficient AS reliabilityCoefficient, " +
            "    eqa.validity_coefficient AS validityCoefficient, " +
            "    eqa.knowledge_coverage_rate AS knowledgeCoverageRate, " +
            "    eqa.overall_difficulty AS overallDifficulty, " +
            "    eqa.overall_discrimination AS overallDiscrimination, " +
            "    eqa.is_abnormal AS isAbnormal, " +
            "    eqa.skewness AS skewness, " +
            "    eqa.kurtosis AS kurtosis, " +
            "    eqa.reliability_evaluation AS reliabilityEvaluation, " +
            "    eqa.difficulty_evaluation AS difficultyEvaluation, " +
            "    eqa.discrimination_evaluation AS discriminationEvaluation " +
            "FROM examination_paper_quality_analysis eqa " +
            "INNER JOIN examination e ON eqa.exam_id = e.exam_id " +
            "INNER JOIN examination_paper ep ON e.paper_id = ep.paper_id " +
            "INNER JOIN course c ON ep.course_id = c.course_id " +
            "<where> " +
            "    <if test='setterId != null'> " +
            "        AND ep.setter_id = #{setterId} " +
            "    </if> " +
            "    <if test='courseId != null'> " +
            "        AND ep.course_id = #{courseId} " +
            "    </if> " +
            "    <if test='paperTitle != null and paperTitle != \"\"'> " +
            "        AND ep.title LIKE CONCAT('%', #{paperTitle}, '%') " +
            "    </if> " +
            "</where> " +
            "ORDER BY eqa.calculated_at DESC" +
            "</script>")
    Page<PaperAnalysisVO> selectPageBySetterId(@Param("page") Page<PaperAnalysisVO> page,
            @Param("setterId") Long setterId, @Param("courseId") Long courseId, @Param("paperTitle") String paperTitle);

    /**
     * 查询试卷分析趋势
     * 
     * @param setterId 命题人ID
     * @param courseId 课程ID
     * @return 试卷分析趋势
     */
    @Select("<script>" +
            "SELECT " +
            "    eqa.analysis_id AS analysisId, " +
            "    eqa.exam_id AS examId, " +
            "    ep.paper_id AS paperId, " +
            "    ep.title AS paperTitle, " +
            "    c.course_name AS courseName, " +
            "    e.exam_date AS examDate, " +
            "    eqa.average_score AS averageScore, " +
            "    eqa.std_deviation AS stdDeviation, " +
            "    eqa.highest_score AS highestScore, " +
            "    eqa.lowest_score AS lowestScore, " +
            "    eqa.reliability_coefficient AS reliabilityCoefficient, " +
            "    eqa.validity_coefficient AS validityCoefficient, " +
            "    eqa.knowledge_coverage_rate AS knowledgeCoverageRate, " +
            "    eqa.overall_difficulty AS overallDifficulty, " +
            "    eqa.overall_discrimination AS overallDiscrimination, " +
            "    eqa.is_abnormal AS isAbnormal, " +
            "    eqa.skewness AS skewness, " +
            "    eqa.kurtosis AS kurtosis, " +
            "    eqa.reliability_evaluation AS reliabilityEvaluation, " +
            "    eqa.difficulty_evaluation AS difficultyEvaluation, " +
            "    eqa.discrimination_evaluation AS discriminationEvaluation " +
            "FROM examination_paper_quality_analysis eqa " +
            "INNER JOIN examination e ON eqa.exam_id = e.exam_id " +
            "INNER JOIN examination_paper ep ON e.paper_id = ep.paper_id " +
            "INNER JOIN course c ON ep.course_id = c.course_id " +
            "<where> " +
            "    <if test='setterId != null'> " +
            "        AND ep.setter_id = #{setterId} " +
            "    </if> " +
            "    <if test='courseId != null'> " +
            "        AND ep.course_id = #{courseId} " +
            "    </if> " +
            "</where> " +
            "ORDER BY e.exam_date ASC" +
            "</script>")
    List<PaperAnalysisVO> selectTrendBySetterId(@Param("setterId") Long setterId, @Param("courseId") Long courseId);
}
