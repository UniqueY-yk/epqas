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

    @PostMapping("/batch")
    public Result<Boolean> batchSaveStudentResult(@RequestBody com.epqas.exam.dto.StudentExamResultBatchDTO dto) {
        return Result.success(studentExamResultService.saveResultAndAnswers(dto));
    }

    @GetMapping("/{id}")
    public Result<StudentExamResult> getStudentExamResultById(@PathVariable("id") Long id) {
        return Result.success(studentExamResultService.getById(id));
    }

    @GetMapping("/exam/{examId}")
    public Result<List<StudentExamResult>> getResultsByExamId(@PathVariable("examId") Long examId) {
        return Result.success(studentExamResultService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentExamResult>().eq("exam_id", examId)));
    }

    @GetMapping("/exam/{examId}/student/{studentId}")
    public Result<StudentExamResult> getResultByExamAndStudent(@PathVariable("examId") Long examId, @PathVariable("studentId") Long studentId) {
        return Result.success(studentExamResultService.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentExamResult>()
                .eq("exam_id", examId)
                .eq("student_id", studentId)));
    }

    @PutMapping
    public Result<Boolean> updateStudentExamResult(@RequestBody StudentExamResult studentExamResult) {
        return Result.success(studentExamResultService.updateById(studentExamResult));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteStudentExamResult(@PathVariable("id") Long id) {
        return Result.success(studentExamResultService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<StudentExamResult>> getStudentExamResultPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(studentExamResultService.page(new Page<>(current, size)));
    }

    @GetMapping
    public Result<List<StudentExamResult>> listStudentExamResults() {
        return Result.success(studentExamResultService.list());
    }
}
