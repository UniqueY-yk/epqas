package com.epqas.analysis.mapper;

import com.epqas.analysis.dto.compute.ComputeExamResultDTO;
import com.epqas.analysis.dto.compute.ComputePaperQuestionDTO;
import com.epqas.analysis.dto.compute.ComputeStudentAnswerDTO;
import com.epqas.analysis.dto.QuestionAnalysisDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamMetricsComputeMapper {
    /**
     * 查询考试结果
     * 
     * @param examId 考试ID
     * @return 考试结果
     */
    List<ComputeExamResultDTO> selectExamResults(@Param("examId") Long examId);

    /**
     * 查询学生答案
     * 
     * @param examId 考试ID
     * @return 学生答案
     */
    List<ComputeStudentAnswerDTO> selectStudentAnswers(@Param("examId") Long examId);

    /**
     * 查询试卷题目
     * 
     * @param examId 考试ID
     * @return 试卷题目
     */
    List<ComputePaperQuestionDTO> selectPaperQuestions(@Param("examId") Long examId);

    /**
     * 统计课程知识点数量
     * 
     * @param courseId 课程ID
     * @return 知识点数量
     */
    Integer countCourseKnowledgePoints(@Param("courseId") Integer courseId);

    /**
     * 统计试卷知识点数量
     * 
     * @param paperId 试卷ID
     * @return 知识点数量
     */
    Integer countPaperKnowledgePoints(@Param("paperId") Long paperId);

    /**
     * 查找有学生结果但尚未进行质量分析的考试ID
     * 
     * @return 尚未进行质量分析的考试ID列表
     */
    List<Long> findPendingExamIdsForAnalysis();

    /**
     * 获取题目级质量分析与题目文本的组合
     * 
     * @param examId 考试ID
     * @return 题目级质量分析与题目文本的组合
     */
    List<QuestionAnalysisDTO> getQuestionAnalysisDetailsByExamId(@Param("examId") Long examId);
}
