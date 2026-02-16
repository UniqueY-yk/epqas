package com.epqas.question.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    public Result<Boolean> createQuestion(@RequestBody Question question) {
        return Result.success(questionService.save(question));
    }

    @GetMapping("/{id}")
    public Result<Question> getQuestionById(@PathVariable Long id) {
        return Result.success(questionService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateQuestion(@RequestBody Question question) {
        return Result.success(questionService.updateById(question));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQuestion(@PathVariable Long id) {
        return Result.success(questionService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<Question>> getQuestionPage(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(questionService.page(new Page<>(current, size)));
    }
    
    @GetMapping
    public Result<List<Question>> listQuestions() {
        return Result.success(questionService.list());
    }
}
