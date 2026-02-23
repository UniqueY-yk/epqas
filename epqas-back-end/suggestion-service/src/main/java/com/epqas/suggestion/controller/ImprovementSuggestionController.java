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

    @PostMapping
    public Result<Boolean> createSuggestion(@RequestBody ImprovementSuggestion suggestion) {
        return Result.success(suggestionService.save(suggestion));
    }

    @GetMapping("/{id}")
    public Result<ImprovementSuggestion> getSuggestionById(@PathVariable("id") Long id) {
        return Result.success(suggestionService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateSuggestion(@RequestBody ImprovementSuggestion suggestion) {
        return Result.success(suggestionService.updateById(suggestion));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSuggestion(@PathVariable("id") Long id) {
        return Result.success(suggestionService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<ImprovementSuggestion>> getSuggestionPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(suggestionService.page(new Page<>(current, size)));
    }

    @GetMapping
    public Result<List<ImprovementSuggestion>> listSuggestions() {
        return Result.success(suggestionService.list());
    }
}
