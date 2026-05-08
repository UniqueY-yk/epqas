package com.epqas.analysis.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.analysis.entity.ExaminationPaperQualityAnalysis;
import com.epqas.analysis.service.ExaminationPaperQualityAnalysisService;
import com.epqas.analysis.service.TrendPredictionService;
import com.epqas.analysis.dto.PaperAnalysisVO;
import com.epqas.analysis.dto.TrendPredictionVO;
import com.epqas.common.result.Result;
import com.epqas.analysis.service.AnalysisComputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/paper-analysis")
public class ExaminationPaperQualityAnalysisController {

    @Autowired
    private ExaminationPaperQualityAnalysisService analysisService;

    @Autowired
    private AnalysisComputationService computationService;

    @Autowired
    private TrendPredictionService trendPredictionService;

    /**
     * 计算试卷指标（按试卷维度，聚合所有使用该试卷的考试实例）
     * 
     * @param paperId 试卷ID
     * @return 计算结果
     */
    @PostMapping("/calculate/{paperId}")
    public Result<Boolean> calculatePaperIndicators(@PathVariable("paperId") Long paperId) {
        computationService.calculatePaperIndicators(paperId);
        return Result.success(true);
    }

    /**
     * 创建分析
     * 
     * @param analysis 分析数据
     * @return 创建结果
     */
    @PostMapping
    public Result<Boolean> createAnalysis(@RequestBody ExaminationPaperQualityAnalysis analysis) {
        return Result.success(analysisService.save(analysis));
    }

    /**
     * 获取分析详情
     * 
     * @param id 分析ID
     * @return 分析详情
     */
    @GetMapping("/{id}")
    public Result<ExaminationPaperQualityAnalysis> getAnalysisById(@PathVariable("id") Long id) {
        return Result.success(analysisService.getById(id));
    }

    /**
     * 更新分析
     * 
     * @param analysis 分析数据
     * @return 更新结果
     */
    @PutMapping
    public Result<Boolean> updateAnalysis(@RequestBody ExaminationPaperQualityAnalysis analysis) {
        return Result.success(analysisService.updateById(analysis));
    }

    /**
     * 删除分析
     * 
     * @param id 分析ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAnalysis(@PathVariable("id") Long id) {
        return Result.success(analysisService.removeById(id));
    }

    /**
     * 获取分析分页数据
     * 
     * @param current 当前页
     * @param size    每页数量
     * @return 分析分页数据
     */
    @GetMapping("/page")
    public Result<Page<ExaminationPaperQualityAnalysis>> getAnalysisPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(analysisService.page(new Page<>(current, size)));
    }

    /**
     * 获取所有分析
     * 
     * @return 分析列表
     */
    @GetMapping
    public Result<List<ExaminationPaperQualityAnalysis>> listAnalysis() {
        return Result.success(analysisService.list());
    }

    /**
     * 获取我的试卷
     * 
     * @param roleId   角色ID
     * @param current  当前页
     * @param size     每页数量
     * @param setterId 命题教师ID
     * @return 试卷分页数据
     */
    @GetMapping("/my-papers")
    public Result<Page<PaperAnalysisVO>> getMyPapers(
            @RequestHeader(value = "X-Role-Id", required = false) Integer roleId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "setterId", required = false) Long setterId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "paperTitle", required = false) String paperTitle) {
        // 管理员(roleId=1)、任课教师(roleId=3)和学生(roleId=4)查看所有试卷；命题教师只查看自己的试卷
        if (roleId != null && (roleId == 1 || roleId == 3 || roleId == 4)) {
            setterId = null;
        }
        return Result.success(analysisService.getPageBySetterId(current, size, setterId, courseId, paperTitle));
    }

    /**
     * 获取趋势分析
     * 必须同时提供 setterId 和 courseId 才能查询趋势数据，
     * 因为一位命题教师可能为多门课程出题，一门课程也可能由多位教师出题。
     *
     * @param roleId   角色ID
     * @param userId   用户ID
     * @param setterId 命题教师ID（必填）
     * @param courseId 课程ID（必填）
     * @return 趋势分析数据
     */
    @GetMapping("/trend")
    public Result<List<PaperAnalysisVO>> getTrendAnalysis(
            @RequestHeader(value = "X-Role-Id", required = false) Integer roleId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(value = "setterId", required = false) Long setterId,
            @RequestParam(value = "courseId") Long courseId) {

        if (roleId != null && (roleId == 1 || roleId == 3 || roleId == 4)) {
            // 管理员、任课教师和学生可以查询任何指定的setterId
        } else {
            // 命题教师只能查询自己的setterId
            setterId = userId;
        }

        // 必须同时提供 setterId 和 courseId
        if (setterId == null || courseId == null) {
            return Result.success(java.util.Collections.emptyList());
        }

        return Result.success(analysisService.getTrendAnalysis(setterId, courseId));
    }

    /**
     * 获取趋势预测
     * 基于历史命题数据，使用加权移动平均或线性回归预测下一套试卷的质量指标。
     * 预测结果会被持久化到 trend_prediction 表。
     *
     * @param roleId   角色ID
     * @param userId   用户ID
     * @param setterId 命题教师ID（必填）
     * @param courseId 课程ID（必填）
     * @return 预测结果
     */
    @GetMapping("/trend/predict")
    public Result<TrendPredictionVO> getTrendPrediction(
            @RequestHeader(value = "X-Role-Id", required = false) Integer roleId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(value = "setterId", required = false) Long setterId,
            @RequestParam(value = "courseId") Long courseId) {

        if (roleId != null && (roleId == 1 || roleId == 3 || roleId == 4)) {
            // 管理员、任课教师和学生可以查询任何指定的setterId
        } else {
            // 命题教师只能查询自己的setterId
            setterId = userId;
        }

        // 必须同时提供 setterId 和 courseId
        if (setterId == null || courseId == null) {
            return Result.success(null);
        }

        TrendPredictionVO prediction = trendPredictionService.predictTrend(setterId, courseId);
        return Result.success(prediction);
    }
}
