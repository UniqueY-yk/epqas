package com.epqas.analysis.mapper;

import com.epqas.analysis.dto.compute.ComputeExamResultDTO;
import com.epqas.analysis.dto.compute.ComputePaperQuestionDTO;
import com.epqas.analysis.dto.compute.ComputeStudentAnswerDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper for directly querying raw exam response data bypassing microservice
 * HTTP overhead
 * since all microservices share the epqas_db database.
 */
@Mapper
public interface ExamMetricsComputeMapper {
    List<ComputeExamResultDTO> selectExamResults(@Param("examId") Long examId);

    List<ComputeStudentAnswerDTO> selectStudentAnswers(@Param("examId") Long examId);

    List<ComputePaperQuestionDTO> selectPaperQuestions(@Param("examId") Long examId);

    Integer countCourseKnowledgePoints(@Param("courseId") Integer courseId);

    Integer countPaperKnowledgePoints(@Param("paperId") Long paperId);

    /**
     * Finds exam IDs that have student results but no corresponding quality
     * analysis yet.
     */
    List<Long> findPendingExamIdsForAnalysis();
}
