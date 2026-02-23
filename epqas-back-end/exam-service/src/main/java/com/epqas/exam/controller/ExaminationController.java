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

    @PostMapping
    public Result<Boolean> createExamination(@RequestBody Examination examination) {
        return Result.success(examinationService.save(examination));
    }

    @GetMapping("/{id}")
    public Result<Examination> getExaminationById(@PathVariable("id") Long id) {
        return Result.success(examinationService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateExamination(@RequestBody Examination examination) {
        return Result.success(examinationService.updateById(examination));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteExamination(@PathVariable("id") Long id) {
        return Result.success(examinationService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<Examination>> getExaminationPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(examinationService.page(new Page<>(current, size)));
    }

    @GetMapping
    public Result<List<Examination>> listExaminations() {
        return Result.success(examinationService.list());
    }
}
