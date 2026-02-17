package com.epqas.academic.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.academic.entity.KnowledgePoint;
import com.epqas.academic.service.KnowledgePointService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/knowledge-points")
public class KnowledgePointController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @GetMapping
    public Result<Page<KnowledgePoint>> listKnowledgePoints(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(knowledgePointService.page(new Page<>(page, size)));
    }

    @PostMapping
    public Result<Boolean> createKnowledgePoint(@RequestBody KnowledgePoint knowledgePoint) {
        return Result.success(knowledgePointService.save(knowledgePoint));
    }

    @PutMapping
    public Result<Boolean> updateKnowledgePoint(@RequestBody KnowledgePoint knowledgePoint) {
        return Result.success(knowledgePointService.updateById(knowledgePoint));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteKnowledgePoint(@PathVariable Integer id) {
        return Result.success(knowledgePointService.removeById(id));
    }
}
