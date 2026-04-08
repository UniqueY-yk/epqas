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

    /**
     * 创建学生答题记录
     * 
     * @param studentAnswer 学生答题记录
     * @return 创建结果
     */
    @PostMapping
    public Result<Boolean> createStudentAnswer(@RequestBody StudentAnswer studentAnswer) {
        return Result.success(studentAnswerService.save(studentAnswer));
    }

    /**
     * 获取学生答题记录详情
     * 
     * @param id 答题记录ID
     * @return 答题记录详情
     */
    @GetMapping("/{id}")
    public Result<StudentAnswer> getStudentAnswerById(@PathVariable("id") Long id) {
        return Result.success(studentAnswerService.getById(id));
    }

    /**
     * 获取某个考试的所有学生答题记录
     * 
     * @param resultId 考试结果ID
     * @return 答题记录列表
     */
    @GetMapping("/result/{resultId}")
    public Result<List<StudentAnswer>> getAnswersByResultId(@PathVariable("resultId") Long resultId) {
        return Result.success(studentAnswerService
                .list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentAnswer>().eq("result_id",
                        resultId)));
    }

    /**
     * 更新学生答题记录
     * 
     * @param studentAnswer 学生答题记录
     * @return 更新结果
     */
    @PutMapping
    public Result<Boolean> updateStudentAnswer(@RequestBody StudentAnswer studentAnswer) {
        return Result.success(studentAnswerService.updateById(studentAnswer));
    }

    /**
     * 删除学生答题记录
     * 
     * @param id 答题记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteStudentAnswer(@PathVariable("id") Long id) {
        return Result.success(studentAnswerService.removeById(id));
    }

    /**
     * 获取学生答题记录分页数据
     * 
     * @param current 当前页
     * @param size    每页数量
     * @return 答题记录分页数据
     */
    @GetMapping("/page")
    public Result<Page<StudentAnswer>> getStudentAnswerPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(studentAnswerService.page(new Page<>(current, size)));
    }

    /**
     * 获取所有学生答题记录
     * 
     * @return 答题记录列表
     */
    @GetMapping
    public Result<List<StudentAnswer>> listStudentAnswers() {
        return Result.success(studentAnswerService.list());
    }
}
