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
     * 查询某试卷所有考试的学生成绩（聚合所有使用该试卷的考试实例）
     * 
     * @param paperId 试卷ID
     * @return 学生考试成绩
     */
    @Select("SELECT r.result_id, r.exam_id, r.student_id, r.total_score, r.is_absent " +
            "FROM student_exam_result r " +
            "INNER JOIN examination e ON r.exam_id = e.exam_id " +
            "WHERE e.paper_id = #{paperId} AND r.is_absent = 0")
    List<ComputeExamResultDTO> selectExamResultsByPaperId(@Param("paperId") Long paperId);

    /**
     * 查询某试卷所有考试的学生答题记录（聚合所有使用该试卷的考试实例）
     * 
     * @param paperId 试卷ID
     * @return 学生答题记录
     */
    @Select("SELECT a.answer_id, a.result_id, a.question_id, r.student_id, " +
            "a.score_obtained, a.is_correct, a.student_choice " +
            "FROM student_answer a " +
            "INNER JOIN student_exam_result r ON a.result_id = r.result_id " +
            "INNER JOIN examination e ON r.exam_id = e.exam_id " +
            "WHERE e.paper_id = #{paperId} AND r.is_absent = 0")
    List<ComputeStudentAnswerDTO> selectStudentAnswersByPaperId(@Param("paperId") Long paperId);

    /**
     * 查询试卷题目（直接通过paperId查询）
     * 
     * @param paperId 试卷ID
     * @return 试卷题目
     */
    @Select("SELECT DISTINCT " +
            "eq.paper_id, eq.question_id, eq.score_value, eq.question_order, q.initial_difficulty " +
            "FROM examination_paper_question eq " +
            "LEFT JOIN question q ON eq.question_id = q.question_id " +
            "WHERE eq.paper_id = #{paperId} " +
            "ORDER BY eq.question_order ASC")
    List<ComputePaperQuestionDTO> selectPaperQuestions(@Param("paperId") Long paperId);

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
     * 查找有学生结果但尚未进行质量分析的试卷ID
     * 
     * @return 尚未进行质量分析的试卷ID列表
     */
    @Select("SELECT DISTINCT ep.paper_id " +
            "FROM examination_paper ep " +
            "INNER JOIN examination e ON ep.paper_id = e.paper_id " +
            "INNER JOIN student_exam_result r ON e.exam_id = r.exam_id " +
            "LEFT JOIN examination_paper_quality_analysis a ON ep.paper_id = a.paper_id " +
            "WHERE a.paper_id IS NULL")
    List<Long> findPendingPaperIdsForAnalysis();

    /**
     * 获取题目级质量分析与题目文本的组合（按试卷ID查询）
     * 
     * @param paperId 试卷ID
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
            "WHERE qa.paper_id = #{paperId} " +
            "ORDER BY qa.question_id ASC")
    List<QuestionAnalysisDTO> getQuestionAnalysisDetailsByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据试卷ID查询课程ID
     * 
     * @param paperId 试卷ID
     * @return 课程ID
     */
    @Select("SELECT course_id FROM examination_paper WHERE paper_id = #{paperId}")
    Integer getCourseIdByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据试卷ID删除改进建议
     * 
     * @param paperId 试卷ID
     */
    @Delete("DELETE FROM improvement_suggestion WHERE paper_id = #{paperId}")
    void deleteSuggestionsByPaperId(@Param("paperId") Long paperId);

    /**
     * 插入改进建议（关联到试卷而非考试）
     * 
     * @param paperId        试卷ID
     * @param questionId     题目ID
     * @param suggestionType 建议类型
     * @param suggestionText 建议内容
     * @param generatedRule  生成规则
     * @param isImplemented  是否已执行
     */
    @Insert("INSERT INTO improvement_suggestion (paper_id, question_id, suggestion_type, suggestion_text, generated_rule, is_implemented) " +
            "VALUES (#{paperId}, #{questionId}, #{suggestionType}, #{suggestionText}, #{generatedRule}, #{isImplemented})")
    void insertImprovementSuggestion(@Param("paperId") Long paperId,
                                     @Param("questionId") Long questionId,
                                     @Param("suggestionType") String suggestionType,
                                     @Param("suggestionText") String suggestionText,
                                     @Param("generatedRule") String generatedRule,
                                     @Param("isImplemented") boolean isImplemented);

    /**
     * 根据试卷ID删除试卷质量分析
     * 
     * @param paperId 试卷ID
     */
    @Delete("DELETE FROM examination_paper_quality_analysis WHERE paper_id = #{paperId}")
    void deletePaperQualityAnalysisByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据试卷ID删除题目质量分析
     * 
     * @param paperId 试卷ID
     */
    @Delete("DELETE FROM question_quality_analysis WHERE paper_id = #{paperId}")
    void deleteQuestionQualityAnalysisByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据试卷ID和题目ID查询改进建议文本
     * 
     * @param paperId    试卷ID
     * @param questionId 题目ID
     * @return 建议文本列表
     */
    @Select("SELECT suggestion_text FROM improvement_suggestion " +
            "WHERE paper_id = #{paperId} AND question_id = #{questionId}")
    List<String> getSuggestionTextsByPaperIdAndQuestionId(@Param("paperId") Long paperId, @Param("questionId") Long questionId);
}
