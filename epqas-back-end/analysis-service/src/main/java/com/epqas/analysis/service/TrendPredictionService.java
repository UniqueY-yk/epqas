package com.epqas.analysis.service;

import com.epqas.analysis.dto.TrendPredictionVO;

public interface TrendPredictionService {

    /**
     * 计算给定教师和课程的趋势预测，持久化结果并返回。
     *
     * @param setterId 教师 ID
     * @param courseId 课程 ID
     * @return 预测结果，或 null（数据不足）
     */
    TrendPredictionVO predictTrend(Long setterId, Long courseId);
}
