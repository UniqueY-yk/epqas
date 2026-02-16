package com.epqas.analysis.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.analysis.entity.QuestionQualityAnalysis;
import com.epqas.analysis.service.QuestionQualityAnalysisService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question-analysis")
public class QuestionQualityAnalysisController {

    @Autowired
    private QuestionQualityAnalysisService analysisService;

    @PostMapping
    public Result<Boolean> createAnalysis(@RequestBody QuestionQualityAnalysis analysis) {
        return Result.success(analysisService.save(analysis));
    }

    @GetMapping("/{id}")
    public Result<QuestionQualityAnalysis> getAnalysisById(@PathVariable Long id) {
        return Result.success(analysisService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateAnalysis(@RequestBody QuestionQualityAnalysis analysis) {
        return Result.success(analysisService.updateById(analysis));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAnalysis(@PathVariable Long id) {
        return Result.success(analysisService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<QuestionQualityAnalysis>> getAnalysisPage(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(analysisService.page(new Page<>(current, size)));
    }
    
    @GetMapping
    public Result<List<QuestionQualityAnalysis>> listAnalysis() {
        return Result.success(analysisService.list());
    }
}
