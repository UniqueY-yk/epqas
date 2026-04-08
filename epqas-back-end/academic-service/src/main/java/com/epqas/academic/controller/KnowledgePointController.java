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

    /**
     * 获取知识点列表
     * 
     * @param page      页码
     * @param size      每页数量
     * @param courseId  课程ID
     * @param pointName 知识点名称
     * @return 知识点列表
     */
    @GetMapping
    public Result<Page<KnowledgePoint>> listKnowledgePoints(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "pointName", required = false) String pointName) {
        return Result.success(knowledgePointService.getKnowledgePointsPage(page, size, courseId, pointName));
    }

    /**
     * 创建知识点
     * 
     * @param knowledgePoint 知识点信息
     * @return 是否创建成功
     */
    @PostMapping
    public Result<Boolean> createKnowledgePoint(@RequestBody KnowledgePoint knowledgePoint) {
        return Result.success(knowledgePointService.save(knowledgePoint));
    }

    /**
     * 更新知识点
     * 
     * @param knowledgePoint 知识点信息
     * @return 是否更新成功
     */
    @PutMapping
    public Result<Boolean> updateKnowledgePoint(@RequestBody KnowledgePoint knowledgePoint) {
        return Result.success(knowledgePointService.updateById(knowledgePoint));
    }

    /**
     * 删除知识点
     * 
     * @param id 知识点ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteKnowledgePoint(@PathVariable("id") Integer id) {
        return Result.success(knowledgePointService.removeById(id));
    }
}
