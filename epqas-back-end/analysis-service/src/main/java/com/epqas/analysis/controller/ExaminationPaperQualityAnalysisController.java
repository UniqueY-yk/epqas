package com.epqas.analysis.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.analysis.entity.ExaminationPaperQualityAnalysis;
import com.epqas.analysis.service.ExaminationPaperQualityAnalysisService;
import com.epqas.analysis.dto.PaperAnalysisVO;
import com.epqas.common.result.Result;
import com.epqas.analysis.service.AnalysisComputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/paper-analysis")
public class ExaminationPaperQualityAnalysisController {

    @Autowired
    private ExaminationPaperQualityAnalysisService analysisService;

    @Autowired
    private AnalysisComputationService computationService;

    @PostMapping("/calculate/{examId}")
    public Result<Boolean> calculateExamIndicators(@PathVariable("examId") Long examId) {
        computationService.calculateExamIndicators(examId);
        return Result.success(true);
    }

    @PostMapping
    public Result<Boolean> createAnalysis(@RequestBody ExaminationPaperQualityAnalysis analysis) {
        return Result.success(analysisService.save(analysis));
    }

    @GetMapping("/{id}")
    public Result<ExaminationPaperQualityAnalysis> getAnalysisById(@PathVariable("id") Long id) {
        return Result.success(analysisService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateAnalysis(@RequestBody ExaminationPaperQualityAnalysis analysis) {
        return Result.success(analysisService.updateById(analysis));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAnalysis(@PathVariable("id") Long id) {
        return Result.success(analysisService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<ExaminationPaperQualityAnalysis>> getAnalysisPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(analysisService.page(new Page<>(current, size)));
    }

    @GetMapping
    public Result<List<ExaminationPaperQualityAnalysis>> listAnalysis() {
        return Result.success(analysisService.list());
    }

    @GetMapping("/my-papers")
    public Result<Page<PaperAnalysisVO>> getMyPapers(
            @RequestHeader(value = "X-Role-Id", required = false) Integer roleId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "setterId", required = false) Long setterId) {
        // Admin (roleId=1) sees all papers; setters see only their own
        if (roleId != null && roleId == 1) {
            setterId = null;
        }
        return Result.success(analysisService.getPageBySetterId(current, size, setterId));
    }

    @GetMapping("/trend")
    public Result<List<PaperAnalysisVO>> getTrendAnalysis(
            @RequestHeader(value = "X-Role-Id", required = false) Integer roleId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(value = "setterId", required = false) Long setterId,
            @RequestParam(value = "courseId", required = false) Long courseId) {
        
        if (roleId != null && roleId == 1) {
            // Admin can query any specified setterId
        } else {
            // Non-admin can ONLY query their own setterId
            setterId = userId;
        }
        return Result.success(analysisService.getTrendAnalysis(setterId, courseId));
    }
}
