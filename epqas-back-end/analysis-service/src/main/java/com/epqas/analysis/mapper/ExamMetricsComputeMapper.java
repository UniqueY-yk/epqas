package com.epqas.analysis.mapper;

import com.epqas.analysis.dto.compute.ComputeExamResultDTO;
import com.epqas.analysis.dto.compute.ComputePaperQuestionDTO;
import com.epqas.analysis.dto.compute.ComputeStudentAnswerDTO;
import com.epqas.analysis.dto.QuestionAnalysisDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamMetricsComputeMapper {
    /**
     * 查询考试结果
     * 
     * @param examId 考试ID
     * @return 考试结果
     */
    @Select("SELECT result_id, exam_id, student_id, total_score, is_absent " +
            "FROM student_exam_result " +
            "WHERE exam_id = #{examId} AND is_absent = 0")
    List<ComputeExamResultDTO> selectExamResults(@Param("examId") Long examId);

    /**
     * 查询学生答案
     * 
     * @param examId 考试ID
     * @return 学生答案
     */
    @Select("SELECT a.answer_id, a.result_id, a.question_id, r.student_id, " +
            "a.score_obtained, a.is_correct, a.student_choice " +
            "FROM student_answer a " +
            "INNER JOIN student_exam_result r ON a.result_id = r.result_id " +
            "WHERE r.exam_id = #{examId} AND r.is_absent = 0")
    List<ComputeStudentAnswerDTO> selectStudentAnswers(@Param("examId") Long examId);

    /**
     * 查询试卷题目
     * 
     * @param examId 考试ID
     * @return 试卷题目
     */
    @Select("SELECT DISTINCT " +
            "eq.paper_id, eq.question_id, eq.score_value, eq.question_order, q.initial_difficulty " +
            "FROM examination_paper_question eq " +
            "INNER JOIN examination e ON eq.paper_id = e.paper_id " +
            "LEFT JOIN question q ON eq.question_id = q.question_id " +
            "WHERE e.exam_id = #{examId} " +
            "ORDER BY eq.question_order ASC")
    List<ComputePaperQuestionDTO> selectPaperQuestions(@Param("examId") Long examId);

    /**
     * 统计课程知识点数量
     * 
     * @param courseId 课程ID
     * @return 知识点数量
     */
    @Select("SELECT COUNT(point_id) FROM knowledge_point WHERE course_id = #{courseId}")
    Integer countCourseKnowledgePoints(@Param("courseId") Integer courseId);

    /**
     * 统计试卷知识点数量
     * 
     * @param paperId 试卷ID
     * @return 知识点数量
     */
    @Select("SELECT COUNT(DISTINCT qkm.point_id) " +
            "FROM examination_paper_question epq " +
            "INNER JOIN question_knowledge_map qkm ON epq.question_id = qkm.question_id " +
            "WHERE epq.paper_id = #{paperId}")
    Integer countPaperKnowledgePoints(@Param("paperId") Long paperId);

    /**
     * 查找有学生结果但尚未进行质量分析的考试ID
     * 
     * @return 尚未进行质量分析的考试ID列表
     */
    @Select("SELECT DISTINCT e.exam_id " +
            "FROM examination e " +
            "INNER JOIN student_exam_result r ON e.exam_id = r.exam_id " +
            "LEFT JOIN examination_paper_quality_analysis a ON e.exam_id = a.exam_id " +
            "WHERE a.exam_id IS NULL")
    List<Long> findPendingExamIdsForAnalysis();

    /**
     * 获取题目级质量分析与题目文本的组合
     * 
     * @param examId 考试ID
     * @return 题目级质量分析与题目文本的组合
     */
    @Select("SELECT " +
            "qa.question_id as questionId, " +
            "q.question_content as stem, " +
            "q.question_type as questionType, " +
            "qa.correct_response_rate as correctResponseRate, " +
            "qa.difficulty_index as difficultyIndex, " +
            "qa.discrimination_index as discriminationIndex, " +
            "qa.validity_index as validityIndex, " +
            "qa.is_too_easy as isTooEasy, " +
            "qa.is_low_discrimination as isLowDiscrimination, " +
            "qa.diagnosis_tag as diagnosisTag, " +
            "qa.selection_distribution_json as selectionDistributionJson, " +
            "q.options_json as optionsJson, " +
            "q.correct_answer as correctAnswer, " +
            "qa.difficulty_evaluation as difficultyEvaluation, " +
            "qa.discrimination_evaluation as discriminationEvaluation, " +
            "(qa.is_too_easy = 1 OR qa.is_low_discrimination = 1) as isAbnormal " +
            "FROM question_quality_analysis qa " +
            "LEFT JOIN question q ON qa.question_id = q.question_id " +
            "WHERE qa.exam_id = #{examId} " +
            "ORDER BY qa.question_id ASC")
    List<QuestionAnalysisDTO> getQuestionAnalysisDetailsByExamId(@Param("examId") Long examId);

    /**
     * 根据试卷ID查询课程ID
     * 
     * @param paperId 试卷ID
     * @return 课程ID
     */
    @Select("SELECT course_id FROM examination_paper WHERE paper_id = #{paperId}")
    Integer getCourseIdByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据考试ID删除改进建议
     * 
     * @param examId 考试ID
     */
    @Delete("DELETE FROM improvement_suggestion WHERE exam_id = #{examId}")
    void deleteSuggestionsByExamId(@Param("examId") Long examId);

    /**
     * 插入改进建议
     * 
     * @param examId         考试ID
     * @param questionId     题目ID
     * @param suggestionType 建议类型
     * @param suggestionText 建议内容
     * @param generatedRule  生成规则
     * @param isImplemented  是否已执行
     */
    @Insert("INSERT INTO improvement_suggestion (exam_id, question_id, suggestion_type, suggestion_text, generated_rule, is_implemented) " +
            "VALUES (#{examId}, #{questionId}, #{suggestionType}, #{suggestionText}, #{generatedRule}, #{isImplemented})")
    void insertImprovementSuggestion(@Param("examId") Long examId,
                                     @Param("questionId") Long questionId,
                                     @Param("suggestionType") String suggestionType,
                                     @Param("suggestionText") String suggestionText,
                                     @Param("generatedRule") String generatedRule,
                                     @Param("isImplemented") boolean isImplemented);

    /**
     * 根据考试ID删除试卷质量分析
     * 
     * @param examId 考试ID
     */
    @Delete("DELETE FROM examination_paper_quality_analysis WHERE exam_id = #{examId}")
    void deletePaperQualityAnalysisByExamId(@Param("examId") Long examId);

    /**
     * 根据考试ID删除题目质量分析
     * 
     * @param examId 考试ID
     */
    @Delete("DELETE FROM question_quality_analysis WHERE exam_id = #{examId}")
    void deleteQuestionQualityAnalysisByExamId(@Param("examId") Long examId);

    /**
     * 根据考试ID和题目ID查询改进建议文本
     * 
     * @param examId     考试ID
     * @param questionId 题目ID
     * @return 建议文本列表
     */
    @Select("SELECT suggestion_text FROM improvement_suggestion " +
            "WHERE exam_id = #{examId} AND question_id = #{questionId}")
    List<String> getSuggestionTextsByExamIdAndQuestionId(@Param("examId") Long examId, @Param("questionId") Long questionId);
}
