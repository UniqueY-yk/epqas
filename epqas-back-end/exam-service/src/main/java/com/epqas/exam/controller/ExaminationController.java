package com.epqas.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.exam.entity.Examination;
import com.epqas.exam.service.ExaminationService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/examinations")
public class ExaminationController {

    @Autowired
    private ExaminationService examinationService;

    /**
     * 创建考试
     * 
     * @param examination 考试信息
     * @return 创建结果
     */
    @PostMapping
    public Result<Boolean> createExamination(@RequestBody Examination examination) {
        return Result.success(examinationService.save(examination));
    }

    /**
     * 获取考试详情
     * 
     * @param id 考试ID
     * @return 考试详情
     */
    @GetMapping("/{id}")
    public Result<Examination> getExaminationById(@PathVariable("id") Long id) {
        return Result.success(examinationService.getById(id));
    }

    /**
     * 更新考试
     * 
     * @param examination 考试信息
     * @return 更新结果
     */
    @PutMapping
    public Result<Boolean> updateExamination(@RequestBody Examination examination) {
        return Result.success(examinationService.updateById(examination));
    }

    /**
     * 删除考试
     * 
     * @param id 考试ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteExamination(@PathVariable("id") Long id) {
        return Result.success(examinationService.removeById(id));
    }

    /**
     * 获取考试分页数据
     * 
     * @param current 当前页
     * @param size    每页数量
     * @return 考试分页数据
     */
    @GetMapping("/page")
    public Result<Page<Examination>> getExaminationPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(examinationService.page(new Page<>(current, size)));
    }

    /**
     * 获取所有考试
     * 
     * @return 考试列表
     */
    @GetMapping
    public Result<List<Examination>> listExaminations() {
        return Result.success(examinationService.list());
    }
}
