package com.epqas.question.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.question.entity.ExaminationPaper;
import com.epqas.question.service.ExaminationPaperService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/papers")
public class ExaminationPaperController {

    @Autowired
    private ExaminationPaperService examinationPaperService;

    @PostMapping
    public Result<Boolean> createExaminationPaper(@RequestBody ExaminationPaper examinationPaper) {
        return Result.success(examinationPaperService.save(examinationPaper));
    }

    @GetMapping("/{id}")
    public Result<ExaminationPaper> getExaminationPaperById(@PathVariable Long id) {
        return Result.success(examinationPaperService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateExaminationPaper(@RequestBody ExaminationPaper examinationPaper) {
        return Result.success(examinationPaperService.updateById(examinationPaper));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteExaminationPaper(@PathVariable Long id) {
        return Result.success(examinationPaperService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<ExaminationPaper>> getExaminationPaperPage(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(examinationPaperService.page(new Page<>(current, size)));
    }
    
    @GetMapping
    public Result<List<ExaminationPaper>> listExaminationPapers() {
        return Result.success(examinationPaperService.list());
    }
}
