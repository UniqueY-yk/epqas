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

    /**
     * 创建试卷
     * 
     * @param examinationPaper 试卷信息
     * @return 是否创建成功
     */
    @PostMapping
    public Result<Boolean> createExaminationPaper(@RequestBody ExaminationPaper examinationPaper) {
        return Result.success(examinationPaperService.save(examinationPaper));
    }

    /**
     * 根据ID获取试卷
     * 
     * @param id 试卷ID
     * @return 试卷信息
     */
    @GetMapping("/{id}")
    public Result<ExaminationPaper> getExaminationPaperById(@PathVariable("id") Long id) {
        return Result.success(examinationPaperService.getById(id));
    }

    /**
     * 更新试卷
     * 
     * @param examinationPaper 试卷信息
     * @return 是否更新成功
     */
    @PutMapping
    public Result<Boolean> updateExaminationPaper(@RequestBody ExaminationPaper examinationPaper) {
        return Result.success(examinationPaperService.updateById(examinationPaper));
    }

    /**
     * 删除试卷
     * 
     * @param id 试卷ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteExaminationPaper(@PathVariable("id") Long id) {
        return Result.success(examinationPaperService.removeById(id));
    }

    /**
     * 分页获取试卷
     * 
     * @param current 当前页
     * @param size    每页数量
     * @return 分页试卷
     */
    @GetMapping("/page")
    public Result<Page<ExaminationPaper>> getExaminationPaperPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(examinationPaperService.page(new Page<>(current, size)));
    }

    /**
     * 获取所有试卷
     * 
     * @return 所有试卷
     */
    @GetMapping
    public Result<List<ExaminationPaper>> listExaminationPapers() {
        return Result.success(examinationPaperService.list());
    }
}
