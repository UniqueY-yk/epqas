package com.epqas.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.exam.entity.StudentAnswer;
import com.epqas.exam.service.StudentAnswerService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/answers")
public class StudentAnswerController {

    @Autowired
    private StudentAnswerService studentAnswerService;

    @PostMapping
    public Result<Boolean> createStudentAnswer(@RequestBody StudentAnswer studentAnswer) {
        return Result.success(studentAnswerService.save(studentAnswer));
    }

    @GetMapping("/{id}")
    public Result<StudentAnswer> getStudentAnswerById(@PathVariable("id") Long id) {
        return Result.success(studentAnswerService.getById(id));
    }

    @GetMapping("/result/{resultId}")
    public Result<List<StudentAnswer>> getAnswersByResultId(@PathVariable("resultId") Long resultId) {
        return Result.success(studentAnswerService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentAnswer>().eq("result_id", resultId)));
    }

    @PutMapping
    public Result<Boolean> updateStudentAnswer(@RequestBody StudentAnswer studentAnswer) {
        return Result.success(studentAnswerService.updateById(studentAnswer));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteStudentAnswer(@PathVariable("id") Long id) {
        return Result.success(studentAnswerService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<StudentAnswer>> getStudentAnswerPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(studentAnswerService.page(new Page<>(current, size)));
    }

    @GetMapping
    public Result<List<StudentAnswer>> listStudentAnswers() {
        return Result.success(studentAnswerService.list());
    }
}
