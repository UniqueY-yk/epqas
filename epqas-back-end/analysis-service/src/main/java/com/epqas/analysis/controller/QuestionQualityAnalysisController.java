package com.epqas.analysis.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.analysis.entity.QuestionQualityAnalysis;
import com.epqas.analysis.service.QuestionQualityAnalysisService;
import com.epqas.common.result.Result;
import com.epqas.analysis.dto.QuestionAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question-analysis")
public class QuestionQualityAnalysisController {

    @Autowired
    private QuestionQualityAnalysisService analysisService;

    @GetMapping("/exam/{examId}")
    public Result<List<QuestionAnalysisDTO>> getQuestionAnalysisByExamId(@PathVariable("examId") Long examId) {
        return Result.success(analysisService.getQuestionAnalysisDetailsByExamId(examId));
    }

    @PostMapping
    public Result<Boolean> createAnalysis(@RequestBody QuestionQualityAnalysis analysis) {
        return Result.success(analysisService.save(analysis));
    }

    @GetMapping("/{id}")
    public Result<QuestionQualityAnalysis> getAnalysisById(@PathVariable("id") Long id) {
        return Result.success(analysisService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateAnalysis(@RequestBody QuestionQualityAnalysis analysis) {
        return Result.success(analysisService.updateById(analysis));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAnalysis(@PathVariable("id") Long id) {
        return Result.success(analysisService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<QuestionQualityAnalysis>> getAnalysisPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(analysisService.page(new Page<>(current, size)));
    }

    @GetMapping
    public Result<List<QuestionQualityAnalysis>> listAnalysis() {
        return Result.success(analysisService.list());
    }
}
