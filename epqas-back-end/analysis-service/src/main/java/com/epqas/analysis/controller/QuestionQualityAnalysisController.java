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

    /**
     * 获取试卷题目分析（按试卷ID查询）
     * 
     * @param paperId 试卷ID
     * @return 题目分析列表
     */
    @GetMapping("/paper/{paperId}")
    public Result<List<QuestionAnalysisDTO>> getQuestionAnalysisByPaperId(@PathVariable("paperId") Long paperId) {
        return Result.success(analysisService.getQuestionAnalysisDetailsByPaperId(paperId));
    }

    /**
     * 创建分析
     * 
     * @param analysis 分析数据
     * @return 创建结果
     */
    @PostMapping
    public Result<Boolean> createAnalysis(@RequestBody QuestionQualityAnalysis analysis) {
        return Result.success(analysisService.save(analysis));
    }

    /**
     * 获取分析详情
     * 
     * @param id 分析ID
     * @return 分析详情
     */
    @GetMapping("/{id}")
    public Result<QuestionQualityAnalysis> getAnalysisById(@PathVariable("id") Long id) {
        return Result.success(analysisService.getById(id));
    }

    /**
     * 更新分析
     * 
     * @param analysis 分析数据
     * @return 更新结果
     */
    @PutMapping
    public Result<Boolean> updateAnalysis(@RequestBody QuestionQualityAnalysis analysis) {
        return Result.success(analysisService.updateById(analysis));
    }

    /**
     * 删除分析
     * 
     * @param id 分析ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAnalysis(@PathVariable("id") Long id) {
        return Result.success(analysisService.removeById(id));
    }

    /**
     * 获取分析分页数据
     * 
     * @param current 当前页
     * @param size    每页数量
     * @return 分析分页数据
     */
    @GetMapping("/page")
    public Result<Page<QuestionQualityAnalysis>> getAnalysisPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(analysisService.page(new Page<>(current, size)));
    }

    /**
     * 获取所有分析
     * 
     * @return 分析列表
     */
    @GetMapping
    public Result<List<QuestionQualityAnalysis>> listAnalysis() {
        return Result.success(analysisService.list());
    }
}
