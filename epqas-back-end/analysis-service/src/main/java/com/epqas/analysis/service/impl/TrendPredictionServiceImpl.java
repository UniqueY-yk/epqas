package com.epqas.analysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.epqas.analysis.dto.PaperAnalysisVO;
import com.epqas.analysis.dto.TrendPredictionVO;
import com.epqas.analysis.entity.TrendPrediction;
import com.epqas.analysis.mapper.TrendPredictionMapper;
import com.epqas.analysis.service.ExaminationPaperQualityAnalysisService;
import com.epqas.analysis.service.TrendPredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendPredictionServiceImpl implements TrendPredictionService {

    private final ExaminationPaperQualityAnalysisService analysisService;
    private final TrendPredictionMapper predictionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrendPredictionVO predictTrend(Long setterId, Long courseId) {
        // 1. 获取历史趋势数据
        List<PaperAnalysisVO> history = analysisService.getTrendAnalysis(setterId, courseId);

        if (history == null || history.size() < 2) {
            log.info("Insufficient data for prediction: setterId={}, courseId={}, records={}",
                    setterId, courseId, history == null ? 0 : history.size());
            return null;
        }

        int n = history.size();
        String method = n < 5 ? "WMA" : "OLS";

        // 2. 提取每个指标的时间序列数据
        float[] difficulties = new float[n];
        float[] discriminations = new float[n];
        float[] reliabilities = new float[n];
        float[] validities = new float[n];

        for (int i = 0; i < n; i++) {
            PaperAnalysisVO item = history.get(i);
            difficulties[i] = item.getOverallDifficulty() != null ? item.getOverallDifficulty() : 0f;
            discriminations[i] = item.getOverallDiscrimination() != null ? item.getOverallDiscrimination() : 0f;
            reliabilities[i] = item.getReliabilityCoefficient() != null ? item.getReliabilityCoefficient() : 0f;
            validities[i] = item.getValidityCoefficient() != null ? item.getValidityCoefficient() : 0f;
        }

        // 3. 计算预测值
        PredictionResult diffResult = predict(difficulties, method);
        PredictionResult discResult = predict(discriminations, method);
        PredictionResult relResult = predict(reliabilities, method);
        PredictionResult valResult = predict(validities, method);

        // 4. 创建实体对象
        TrendPrediction entity = new TrendPrediction();
        entity.setSetterId(setterId);
        entity.setCourseId(courseId.intValue());
        entity.setDataPointsUsed(n);
        entity.setMethod(method);

        entity.setPredictedDifficulty(diffResult.predicted);
        entity.setPredictedDiscrimination(discResult.predicted);
        entity.setPredictedReliability(relResult.predicted);
        entity.setPredictedValidity(valResult.predicted);

        entity.setDifficultyLower(diffResult.lower);
        entity.setDifficultyUpper(diffResult.upper);
        entity.setDiscriminationLower(discResult.lower);
        entity.setDiscriminationUpper(discResult.upper);
        entity.setReliabilityLower(relResult.lower);
        entity.setReliabilityUpper(relResult.upper);
        entity.setValidityLower(valResult.lower);
        entity.setValidityUpper(valResult.upper);

        entity.setDifficultyTrend(describeTrendDifficulty(diffResult.slope));
        entity.setDiscriminationTrend(describeTrendGeneral(discResult.slope, "区分度"));
        entity.setReliabilityTrend(describeTrendGeneral(relResult.slope, "信度"));
        entity.setValidityTrend(describeTrendGeneral(valResult.slope, "效度"));

        // 5. 插入或更新预测记录
        LambdaQueryWrapper<TrendPrediction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrendPrediction::getSetterId, setterId)
               .eq(TrendPrediction::getCourseId, courseId.intValue());
        predictionMapper.delete(wrapper);
        predictionMapper.insert(entity);

        // 6. 将实体对象转换为VO并返回
        TrendPredictionVO vo = new TrendPredictionVO();
        BeanUtils.copyProperties(entity, vo);
        // 重新读取实体对象，获取自动生成的predicted时间戳
        TrendPrediction saved = predictionMapper.selectById(entity.getPredictionId());
        if (saved != null) {
            vo.setPredictedAt(saved.getPredictedAt());
            vo.setPredictionId(saved.getPredictionId());
        }
        return vo;
    }

    // ==================== 预测算法 ====================

    /**
     * 根据方法字符串选择合适的预测算法。
     * @param series 数据序列
     * @param method 预测方法（WMA或OLS）
     * @return 预测结果
     */
    private PredictionResult predict(float[] series, String method) {
        if ("WMA".equals(method)) {
            return weightedMovingAverage(series);
        } else {
            return linearRegression(series);
        }
    }

    /**
     * 加权移动平均法 (WMA)。
     * 权重： [1, 2, 3, ..., n] — 更近数据的权重更高。
     */
    private PredictionResult weightedMovingAverage(float[] series) {
        int n = series.length;
        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (int i = 0; i < n; i++) {
            double w = i + 1; // 更近数据的权重更高
            weightedSum += w * series[i];
            totalWeight += w;
        }

        float predicted = clamp((float) (weightedSum / totalWeight));

        // 计算标准误差（信区间）
        double sumSqDiff = 0.0;
        for (int i = 0; i < n; i++) {
            sumSqDiff += Math.pow(series[i] - predicted, 2);
        }
        float sigma = (float) Math.sqrt(sumSqDiff / n);

        // 斜率近似方法：最后一个值与第一个值的差值
        float slope = (series[n - 1] - series[0]) / Math.max(n - 1, 1);

        return new PredictionResult(
                predicted,
                clamp(predicted - sigma),
                clamp(predicted + sigma),
                slope
        );
    }

    /**
     * 最小角线性回归 (OLS)。
     * X = [1, 2, ..., n], Y = 数据值。
     * 预测值：X = n+1。
     */
    private PredictionResult linearRegression(float[] series) {
        int n = series.length;

        // 计算均值
        double meanX = 0.0;
        double meanY = 0.0;
        for (int i = 0; i < n; i++) {
            meanX += (i + 1);
            meanY += series[i];
        }
        meanX /= n;
        meanY /= n;

        // 计算斜率 (b) 和截距 (a)
        double numerator = 0.0;
        double denominator = 0.0;
        for (int i = 0; i < n; i++) {
            double xi = i + 1;
            double yi = series[i];
            numerator += (xi - meanX) * (yi - meanY);
            denominator += (xi - meanX) * (xi - meanX);
        }

        double b = denominator != 0 ? numerator / denominator : 0.0; // 斜率
        double a = meanY - b * meanX; // 截距

        // 预测值：X = n + 1
        float predicted = clamp((float) (a + b * (n + 1)));

        // 估计标准误差
        double sumSqResiduals = 0.0;
        for (int i = 0; i < n; i++) {
            double fitted = a + b * (i + 1);
            sumSqResiduals += Math.pow(series[i] - fitted, 2);
        }
        float sigma = (float) Math.sqrt(sumSqResiduals / Math.max(n - 2, 1));

        return new PredictionResult(
                predicted,
                clamp(predicted - sigma),
                clamp(predicted + sigma),
                (float) b
        );
    }

    // ==================== Trend Description ====================

    /**
     * 描述难度趋势。对于难度，越高越易。
     */
    private String describeTrendDifficulty(float slope) {
        if (slope > 0.02f) {
            return "上升趋势（试卷趋易）";
        } else if (slope < -0.02f) {
            return "下降趋势（试卷趋难）";
        } else {
            return "基本持平";
        }
    }

    /**
     * 描述一般趋势。
     * 对于这些指标，越高越好。
    
     * 描述区分度、可靠性、效度趋势。
     * 对于这些指标，较高值通常更好。
     */
    private String describeTrendGeneral(float slope, String indicatorName) {
        if (slope > 0.02f) {
            return "上升趋势（" + indicatorName + "提升）";
        } else if (slope < -0.02f) {
            return "下降趋势（" + indicatorName + "下降）";
        } else {
            return "基本持平";
        }
    }

    // ==================== 工具函数 ====================

    /**
     * 将值限制在 [0, 1]范围内，因为所有指标都是0-1系数。
     */
    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    /**
     * 预测结果持有者，用于存储单个指标的预测结果。
     */
    private static class PredictionResult {
        final float predicted;
        final float lower;
        final float upper;
        final float slope;

        PredictionResult(float predicted, float lower, float upper, float slope) {
            this.predicted = predicted;
            this.lower = lower;
            this.upper = upper;
            this.slope = slope;
        }
    }
}
