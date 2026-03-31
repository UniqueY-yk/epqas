package com.epqas.exam.controller;

import com.epqas.exam.dto.QuestionStatsDTO;
import com.epqas.exam.dto.KnowledgeMasteryDTO;
import com.epqas.exam.dto.AbnormalDetectionDTO;
import com.epqas.exam.service.ClassAnalysisService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/class-analysis")
public class ClassAnalysisController {

    @Autowired
    private ClassAnalysisService classAnalysisService;

    @GetMapping("/{examId}/questions")
    public Result<List<QuestionStatsDTO>> getQuestionStats(@PathVariable("examId") Long examId) {
        return Result.success(classAnalysisService.getQuestionStats(examId));
    }

    @GetMapping("/{examId}/knowledge-mastery")
    public Result<List<KnowledgeMasteryDTO>> getKnowledgeMastery(@PathVariable("examId") Long examId) {
        return Result.success(classAnalysisService.getKnowledgeMastery(examId));
    }

    @GetMapping("/{examId}/abnormal-detection")
    public Result<List<AbnormalDetectionDTO>> detectAbnormalAnswers(@PathVariable("examId") Long examId) {
        return Result.success(classAnalysisService.detectAbnormalAnswers(examId));
    }
}
