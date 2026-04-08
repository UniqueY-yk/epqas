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

    /**
     * 创建学生考试结果
     * 
     * @param studentExamResult 学生考试结果
     * @return 创建结果
     */
    @PostMapping
    public Result<Boolean> createStudentExamResult(@RequestBody StudentExamResult studentExamResult) {
        return Result.success(studentExamResultService.save(studentExamResult));
    }

    /**
     * 批量保存学生考试结果
     * 
     * @param dto 批量考试结果数据
     * @return 保存结果
     */
    @PostMapping("/batch")
    public Result<Boolean> batchSaveStudentResult(@RequestBody com.epqas.exam.dto.StudentExamResultBatchDTO dto) {
        return Result.success(studentExamResultService.saveResultAndAnswers(dto));
    }

    /**
     * 获取学生考试结果详情
     * 
     * @param id 考试结果ID
     * @return 考试结果详情
     */
    @GetMapping("/{id}")
    public Result<StudentExamResult> getStudentExamResultById(@PathVariable("id") Long id) {
        return Result.success(studentExamResultService.getById(id));
    }

    /**
     * 获取某个考试的所有学生考试结果
     * 
     * @param examId 考试ID
     * @return 考试结果列表
     */
    @GetMapping("/exam/{examId}")
    public Result<List<StudentExamResult>> getResultsByExamId(@PathVariable("examId") Long examId) {
        return Result.success(studentExamResultService
                .list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentExamResult>().eq("exam_id",
                        examId)));
    }

    /**
     * 获取某个考试的某个学生的考试结果
     * 
     * @param examId    考试ID
     * @param studentId 学生ID
     * @return 考试结果
     */
    @GetMapping("/exam/{examId}/student/{studentId}")
    public Result<StudentExamResult> getResultByExamAndStudent(@PathVariable("examId") Long examId,
            @PathVariable("studentId") Long studentId) {
        return Result.success(studentExamResultService
                .getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentExamResult>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId)));
    }

    /**
     * 更新学生考试结果
     * 
     * @param studentExamResult 学生考试结果
     * @return 更新结果
     */
    @PutMapping
    public Result<Boolean> updateStudentExamResult(@RequestBody StudentExamResult studentExamResult) {
        return Result.success(studentExamResultService.updateById(studentExamResult));
    }

    /**
     * 删除学生考试结果
     * 
     * @param id 考试结果ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteStudentExamResult(@PathVariable("id") Long id) {
        return Result.success(studentExamResultService.removeById(id));
    }

    /**
     * 获取学生考试结果分页数据
     * 
     * @param current 当前页
     * @param size    每页数量
     * @return 考试结果分页数据
     */
    @GetMapping("/page")
    public Result<Page<StudentExamResult>> getStudentExamResultPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(studentExamResultService.page(new Page<>(current, size)));
    }

    /**
     * 获取所有学生考试结果
     * 
     * @return 考试结果列表
     */
    @GetMapping
    public Result<List<StudentExamResult>> listStudentExamResults() {
        return Result.success(studentExamResultService.list());
    }

    /**
     * 获取学生所有考试结果
     * 
     * @param studentId 学生ID
     * @return 考试结果列表
     */
    @GetMapping("/student/{studentId}/scores")
    public Result<List<StudentExamResult>> getStudentScores(@PathVariable("studentId") Long studentId) {
        List<StudentExamResult> results = studentExamResultService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentExamResult>()
                        .eq("student_id", studentId)
                        .orderByDesc("submitted_at"));
        return Result.success(results);
    }
}
