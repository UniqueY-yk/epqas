package com.epqas.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.common.result.Result;
import com.epqas.exam.dto.ExaminationPaperDTO;
import com.epqas.exam.service.ExaminationPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/papers")
public class ExaminationPaperController {

    @Autowired
    private ExaminationPaperService paperService;

    @PostMapping
    public Result createPaper(@RequestBody ExaminationPaperDTO dto) {
        // Here we could get setterId from the token context in a real scenario
        paperService.createPaperWithQuestions(dto);
        return Result.success("Paper created successfully");
    }

    @PutMapping
    public Result updatePaper(@RequestBody ExaminationPaperDTO dto) {
        paperService.updatePaperWithQuestions(dto);
        return Result.success("Paper updated successfully");
    }

    @GetMapping("/{id}")
    public Result getPaper(@PathVariable("id") Long id) {
        ExaminationPaperDTO dto = paperService.getPaperWithQuestions(id);
        if (dto != null) {
            return Result.success(dto);
        }
        return Result.error("Paper not found");
    }

    @DeleteMapping("/{id}")
    public Result deletePaper(@PathVariable("id") Long id) {
        // Could also physically delete questions mapping, but leaving it simple for now
        paperService.removeById(id);
        return Result.success("Paper deleted successfully");
    }

    @GetMapping("/page")
    public Result getPaperPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<ExaminationPaperDTO> page = paperService.getPaperPage(current, size, courseId, keyword);
        return Result.success(page);
    }
}
