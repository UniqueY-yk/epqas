package com.epqas.question.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.question.dto.QuestionDTO;
import com.epqas.question.entity.Question;
import com.epqas.question.service.QuestionService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 创建题目
     * 
     * @param dto 题目信息
     * @return 是否创建成功
     */
    @PostMapping
    public Result<Boolean> createQuestion(@RequestBody QuestionDTO dto) {
        questionService.createQuestionWithPoints(dto);
        return Result.success(true);
    }

    /**
     * 根据ID获取题目
     * 
     * @param id 题目ID
     * @return 题目信息
     */
    @GetMapping("/{id}")
    public Result<QuestionDTO> getQuestionById(@PathVariable("id") Long id) {
        return Result.success(questionService.getQuestionById(id));
    }

    /**
     * 更新题目
     * 
     * @param dto 题目信息
     * @return 是否更新成功
     */
    @PutMapping
    public Result<Boolean> updateQuestion(@RequestBody QuestionDTO dto) {
        questionService.updateQuestionWithPoints(dto);
        return Result.success(true);
    }

    /**
     * 删除题目
     * 
     * @param id 题目ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQuestion(@PathVariable("id") Long id) {
        return Result.success(questionService.removeById(id));
    }

    /**
     * 分页获取题目
     * 
     * @param current  当前页
     * @param size     每页数量
     * @param courseId 课程ID
     * @param type     题目类型
     * @param keyword  关键词
     * @return 分页题目
     */
    @GetMapping("/page")
    public Result<Page<Question>> getQuestionPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(questionService.getQuestionPage(current, size, courseId, type, keyword));
    }

    /**
     * 根据ID列表获取题目
     * 
     * @param ids 题目ID列表
     * @return 题目列表
     */
    @GetMapping("/batch")
    public Result<List<Question>> getQuestionsByIds(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        return Result.success(questionService.listByIds(ids));
    }

    /**
     * 获取所有题目
     * 
     * @return 题目列表
     */
    @GetMapping
    public Result<List<Question>> listQuestions() {
        return Result.success(questionService.list());
    }
}
