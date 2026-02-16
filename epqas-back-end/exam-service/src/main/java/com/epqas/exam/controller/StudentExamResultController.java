package com.epqas.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.service.StudentExamResultService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/results")
public class StudentExamResultController {

    @Autowired
    private StudentExamResultService studentExamResultService;

    @PostMapping
    public Result<Boolean> createStudentExamResult(@RequestBody StudentExamResult studentExamResult) {
        return Result.success(studentExamResultService.save(studentExamResult));
    }

    @GetMapping("/{id}")
    public Result<StudentExamResult> getStudentExamResultById(@PathVariable Long id) {
        return Result.success(studentExamResultService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateStudentExamResult(@RequestBody StudentExamResult studentExamResult) {
        return Result.success(studentExamResultService.updateById(studentExamResult));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteStudentExamResult(@PathVariable Long id) {
        return Result.success(studentExamResultService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<StudentExamResult>> getStudentExamResultPage(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(studentExamResultService.page(new Page<>(current, size)));
    }
    
    @GetMapping
    public Result<List<StudentExamResult>> listStudentExamResults() {
        return Result.success(studentExamResultService.list());
    }
}
