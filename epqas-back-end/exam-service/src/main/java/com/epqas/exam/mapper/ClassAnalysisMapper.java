package com.epqas.exam.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * 班级分析特化Mapper，用于处理复杂的分析查询
 */
@Mapper
public interface ClassAnalysisMapper {

    /**
     * 根据题目ID列表查询题目详情
     * 
     * @param questionIds 题目ID列表
     * @return 题目详情列表
     */
    @Select("<script>" +
            "SELECT question_id, question_content, question_type, correct_answer, options_json " +
            "FROM question WHERE question_id IN " +
            "<foreach item='id' collection='questionIds' open='(' separator=',' close=')'>" +
            "  #{id}" +
            "</foreach>" +
            "</script>")
    List<Map<String, Object>> selectQuestionDetailsByIds(@Param("questionIds") List<Long> questionIds);

    /**
     * 根据题目ID列表查询知识点名称
     * 
     * @param questionIds 题目ID列表
     * @return 题目-知识点映射列表
     */
    @Select("<script>" +
            "SELECT qkp.question_id, kp.point_id, kp.point_name " +
            "FROM question_knowledge_map qkp " +
            "JOIN knowledge_point kp ON qkp.point_id = kp.point_id " +
            "WHERE qkp.question_id IN " +
            "<foreach item='id' collection='questionIds' open='(' separator=',' close=')'>" +
            "  #{id}" +
            "</foreach>" +
            "</script>")
    List<Map<String, Object>> selectKnowledgePointsByQuestionIds(@Param("questionIds") List<Long> questionIds);

    /**
     * 根据学生ID列表查询学生姓名 (关联User表)
     * 
     * @param studentIds 学生ID列表
     * @return 学生ID-真实姓名映射列表
     */
    @Select("<script>" +
            "SELECT s.student_id, u.real_name FROM student s " +
            "JOIN user u ON s.user_id = u.user_id " +
            "WHERE s.student_id IN " +
            "<foreach item='id' collection='studentIds' open='(' separator=',' close=')'>" +
            "  #{id}" +
            "</foreach>" +
            "</script>")
    List<Map<String, Object>> selectStudentNamesByIds(@Param("studentIds") List<Long> studentIds);
}
