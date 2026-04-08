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

    /**
     * 获取试卷题目统计信息
     * 
     * @param examId 考试ID
     * @return 题目统计信息列表
     */
    @GetMapping("/{examId}/questions")
    public Result<List<QuestionStatsDTO>> getQuestionStats(@PathVariable("examId") Long examId) {
        return Result.success(classAnalysisService.getQuestionStats(examId));
    }

    /**
     * 获取知识点掌握情况
     * 
     * @param examId 考试ID
     * @return 知识点掌握情况列表
     */
    @GetMapping("/{examId}/knowledge-mastery")
    public Result<List<KnowledgeMasteryDTO>> getKnowledgeMastery(@PathVariable("examId") Long examId) {
        return Result.success(classAnalysisService.getKnowledgeMastery(examId));
    }

    /**
     * 检测异常答案
     * 
     * @param examId 考试ID
     * @return 异常答案列表
     */
    @GetMapping("/{examId}/abnormal-detection")
    public Result<List<AbnormalDetectionDTO>> detectAbnormalAnswers(@PathVariable("examId") Long examId) {
        return Result.success(classAnalysisService.detectAbnormalAnswers(examId));
    }
}
