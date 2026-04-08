package com.epqas.suggestion.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.suggestion.entity.ImprovementSuggestion;
import com.epqas.suggestion.service.ImprovementSuggestionService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suggestions")
public class ImprovementSuggestionController {

    @Autowired
    private ImprovementSuggestionService suggestionService;

    /**
     * 创建建议
     * 
     * @param suggestion 建议
     * @return 创建结果
     */
    @PostMapping
    public Result<Boolean> createSuggestion(@RequestBody ImprovementSuggestion suggestion) {
        return Result.success(suggestionService.save(suggestion));
    }

    /**
     * 获取建议详情
     * 
     * @param id 建议ID
     * @return 建议详情
     */
    @GetMapping("/{id}")
    public Result<ImprovementSuggestion> getSuggestionById(@PathVariable("id") Long id) {
        return Result.success(suggestionService.getById(id));
    }

    /**
     * 更新建议
     * 
     * @param suggestion 建议
     * @return 更新结果
     */
    @PutMapping
    public Result<Boolean> updateSuggestion(@RequestBody ImprovementSuggestion suggestion) {
        return Result.success(suggestionService.updateById(suggestion));
    }

    /**
     * 删除建议
     * 
     * @param id 建议ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSuggestion(@PathVariable("id") Long id) {
        return Result.success(suggestionService.removeById(id));
    }

    /**
     * 获取建议分页数据
     * 
     * @param current 当前页
     * @param size    每页数量
     * @return 建议分页数据
     */
    @GetMapping("/page")
    public Result<Page<ImprovementSuggestion>> getSuggestionPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(suggestionService.page(new Page<>(current, size)));
    }

    /**
     * 获取建议列表
     * 
     * @param examId     考试ID
     * @param questionId 题目ID
     * @return 建议列表
     */
    @GetMapping
    public Result<List<ImprovementSuggestion>> listSuggestions(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long questionId) {
        return Result.success(suggestionService.getSuggestionsByExamAndQuestion(examId, questionId));
    }
}
