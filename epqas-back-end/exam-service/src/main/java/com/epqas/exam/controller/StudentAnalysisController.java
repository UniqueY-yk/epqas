package com.epqas.exam.controller;

import com.epqas.exam.dto.KnowledgeMasteryDTO;
import com.epqas.exam.service.StudentAnalysisService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student-analysis")
public class StudentAnalysisController {

    @Autowired
    private StudentAnalysisService studentAnalysisService;

    /**
     * 获取学生知识掌握情况
     * 
     * @param studentId 学生ID
     * @return 知识掌握情况列表
     */
    @GetMapping("/{studentId}/knowledge-mastery")
    public Result<List<KnowledgeMasteryDTO>> getStudentKnowledgeMastery(@PathVariable("studentId") Long studentId) {
        return Result.success(studentAnalysisService.getStudentKnowledgeMastery(studentId));
    }

    /**
     * 获取学生错题列表
     * 
     * @param studentId 学生ID
     * @param query     查询条件
     * @return 错题列表
     */
    @GetMapping("/{studentId}/error-questions")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.epqas.exam.dto.StudentErrorQuestionDTO>> getStudentErrorQuestions(
            @PathVariable("studentId") Long studentId,
            @ModelAttribute com.epqas.exam.dto.StudentErrorQuestionQuery query) {
        query.setStudentId(studentId);
        return Result.success(studentAnalysisService.getStudentErrorQuestions(query));
    }
}
