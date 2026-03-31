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

    @GetMapping("/{studentId}/knowledge-mastery")
    public Result<List<KnowledgeMasteryDTO>> getStudentKnowledgeMastery(@PathVariable("studentId") Long studentId) {
        return Result.success(studentAnalysisService.getStudentKnowledgeMastery(studentId));
    }

    @GetMapping("/{studentId}/error-questions")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.epqas.exam.dto.StudentErrorQuestionDTO>> getStudentErrorQuestions(
            @PathVariable("studentId") Long studentId, 
            @ModelAttribute com.epqas.exam.dto.StudentErrorQuestionQuery query) {
        query.setStudentId(studentId);
        return Result.success(studentAnalysisService.getStudentErrorQuestions(query));
    }
}
