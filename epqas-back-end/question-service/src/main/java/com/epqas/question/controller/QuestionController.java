package com.epqas.question.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.question.dto.QuestionDTO;
import com.epqas.question.entity.Question;
import com.epqas.question.service.QuestionService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public Result<Boolean> createQuestion(@RequestBody QuestionDTO dto) {
        questionService.createQuestionWithPoints(dto);
        return Result.success(true);
    }

    @GetMapping("/{id}")
    public Result<QuestionDTO> getQuestionById(@PathVariable("id") Long id) {
        return Result.success(questionService.getQuestionById(id));
    }

    @PutMapping
    public Result<Boolean> updateQuestion(@RequestBody QuestionDTO dto) {
        questionService.updateQuestionWithPoints(dto);
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQuestion(@PathVariable("id") Long id) {
        return Result.success(questionService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<Question>> getQuestionPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(questionService.getQuestionPage(current, size, courseId, type, keyword));
    }

    @GetMapping
    public Result<List<Question>> listQuestions() {
        return Result.success(questionService.list());
    }
}
