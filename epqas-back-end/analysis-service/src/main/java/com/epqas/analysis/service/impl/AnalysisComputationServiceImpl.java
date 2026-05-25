package com.epqas.analysis.service.impl;

import com.epqas.common.utils.AESUtil;

import com.epqas.analysis.dto.compute.ComputeExamResultDTO;
import com.epqas.analysis.dto.compute.ComputePaperQuestionDTO;
import com.epqas.analysis.dto.compute.ComputeStudentAnswerDTO;
import com.epqas.analysis.entity.ExaminationPaperQualityAnalysis;
import com.epqas.analysis.entity.QuestionQualityAnalysis;
import com.epqas.analysis.mapper.ExamMetricsComputeMapper;
import com.epqas.analysis.service.AnalysisComputationService;
import com.epqas.analysis.service.ExaminationPaperQualityAnalysisService;
import com.epqas.analysis.service.QuestionQualityAnalysisService;
import com.epqas.analysis.dto.QuestionAnalysisDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisComputationServiceImpl implements AnalysisComputationService {

    private final ExamMetricsComputeMapper computeMapper;
    private final ExaminationPaperQualityAnalysisService examAnalysisService;
    private final QuestionQualityAnalysisService questionAnalysisService;

    /**
     * 计算试卷指标（按试卷维度，聚合所有使用该试卷的考试实例）
     * 
     * @param paperId 试卷ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculatePaperIndicators(Long paperId) {
        log.info("Starting indicator computation for paperId: {}", paperId);

        // ==================== 1. 加载上下文数据（聚合所有考试实例） ====================
        List<ComputeExamResultDTO> results = computeMapper.selectExamResultsByPaperId(paperId);
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("没有找到学生成绩数据，无法进行计算");
        }

        List<ComputeStudentAnswerDTO> answers = computeMapper.selectStudentAnswersByPaperId(paperId);
        List<ComputePaperQuestionDTO> questions = computeMapper.selectPaperQuestions(paperId);

        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("没有找到试卷题目数据，无法进行计算");
        }

        // ==================== 2. 基础统计 ====================
        Integer courseId = computeMapper.getCourseIdByPaperId(paperId);

        // 计算基本统计 (平均分, 最高分, 最低分, 标准差)
        BigDecimal sumScores = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal minScore = new BigDecimal("9999");
        List<BigDecimal> scoreList = new ArrayList<>();

        for (ComputeExamResultDTO r : results) {
            BigDecimal ts = r.getTotalScore() != null ? r.getTotalScore() : BigDecimal.ZERO;
            scoreList.add(ts);
            sumScores = sumScores.add(ts);
            if (ts.compareTo(maxScore) > 0)
                maxScore = ts;
            if (ts.compareTo(minScore) < 0)
                minScore = ts;
        }

        int N = results.size(); // 总学生数
        BigDecimal avgScore = sumScores.divide(BigDecimal.valueOf(N), 4, RoundingMode.HALF_UP);
        double totalScoreVariance = calculateVariance(scoreList, avgScore); // S_X²

        // ==================== 3. 极端组分组 (27%) ====================
        results.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore())); // 降序
        int groupSize = (int) Math.round(N * 0.27); // 取27%，四舍五入
        if (groupSize < 1)
            groupSize = 1;

        List<ComputeExamResultDTO> highGroup = results.subList(0, Math.min(groupSize, N));
        List<ComputeExamResultDTO> lowGroup = results.subList(Math.max(0, N - groupSize), N);

        Set<Long> highGroupIds = highGroup.stream().map(ComputeExamResultDTO::getStudentId).collect(Collectors.toSet());
        Set<Long> lowGroupIds = lowGroup.stream().map(ComputeExamResultDTO::getStudentId).collect(Collectors.toSet());

        // ==================== 4. 按题目分组答题记录 ====================
        Map<Long, List<ComputeStudentAnswerDTO>> answersByQuestion = answers.stream()
                .collect(Collectors.groupingBy(ComputeStudentAnswerDTO::getQuestionId));

        // 为Pearson相关系数准备: 每个学生的总分Map
        Map<Long, BigDecimal> studentTotalScores = new HashMap<>();
        for (ComputeExamResultDTO r : results) {
            studentTotalScores.put(r.getStudentId(), r.getTotalScore() != null ? r.getTotalScore() : BigDecimal.ZERO);
        }
        double meanTotal = avgScore.doubleValue();

        // ==================== 5. 知识覆盖率 ====================
        Integer totalCoursePoints = computeMapper.countCourseKnowledgePoints(courseId);
        Integer paperPoints = computeMapper.countPaperKnowledgePoints(paperId);
        double coverageRate = 0.0;
        if (totalCoursePoints != null && totalCoursePoints > 0 && paperPoints != null) {
            coverageRate = (double) paperPoints / totalCoursePoints;
        }

        // ==================== 6. 信度计算准备 ====================
        // 整体试卷信度将在逐题 r_i 计算后，通过 calculatePaperReliability 方法得到
        int K = questions.size();

        // ==================== 7. 逐题指标计算 (q_i, P, d_i, V) ====================
        deleteExistingItemAnalysis(paperId);
        List<QuestionQualityAnalysis> qAnalyses = new ArrayList<>();
        double sumV = 0.0; // 用于计算平均效度

        for (ComputePaperQuestionDTO q : questions) {
            QuestionQualityAnalysis itemQa = new QuestionQualityAnalysis();
            itemQa.setPaperId(paperId);
            itemQa.setQuestionId(q.getQuestionId());

            List<ComputeStudentAnswerDTO> qAns = answersByQuestion.getOrDefault(q.getQuestionId(), new ArrayList<>());
            BigDecimal H = q.getScoreValue(); // 该题满分
            if (H == null || H.compareTo(BigDecimal.ZERO) == 0)
                H = BigDecimal.ONE;

            // ---- 7a. CTT单题难度 q_i = 1 - (该题平均得分 / 该题满分) ----
            float questionDifficulty = calculateQuestionDifficulty(qAns, H, N);
            itemQa.setCorrectResponseRate(questionDifficulty);

            // ---- 7b. 极端组法难度 P = (X_H + X_L) / (2 * groupSize * H) ----
            BigDecimal xH = BigDecimal.ZERO; // 高分组该题得分总和
            BigDecimal xL = BigDecimal.ZERO; // 低分组该题得分总和
            for (ComputeStudentAnswerDTO a : qAns) {
                BigDecimal score = a.getScoreObtained() != null ? a.getScoreObtained() : BigDecimal.ZERO;
                if (highGroupIds.contains(a.getStudentId())) {
                    xH = xH.add(score);
                }
                if (lowGroupIds.contains(a.getStudentId())) {
                    xL = xL.add(score);
                }
            }
            double denomP = 2.0 * groupSize * H.doubleValue();
            double P = denomP > 0 ? (xH.doubleValue() + xL.doubleValue()) / denomP : 0.0;
            P = Math.max(0.0, Math.min(1.0, P));
            itemQa.setDifficultyIndex((float) P);

            // ---- 7c. CTT区分度 d_i = (H_i - L_i) / W_i ----
            float di = calculateQuestionDiscrimination(qAns, highGroupIds, lowGroupIds, H);
            itemQa.setDiscriminationIndex(di);

            // ---- 7d. CTT单题信度 r_i (Cronbach's Alpha) ----
            float ri = calculateQuestionReliability(questions, answersByQuestion, N, totalScoreVariance);
            itemQa.setReliabilityCoefficient(ri);

            // ---- 7e. 效度 Pearson r(i,T) ----
            double validity = calculatePearsonCorrelation(qAns, studentTotalScores, N, meanTotal);
            itemQa.setValidityIndex((float) validity);
            sumV += Math.abs(validity);

            // ---- 7e. 选项分布 ----
            Map<String, Integer> choiceCounts = new HashMap<>();
            for (ComputeStudentAnswerDTO a : qAns) {
                // student_choice is encrypted in DB; raw SQL bypasses TypeHandler, so decrypt here
                String rawChoice = a.getStudentChoice();
                String choice = null;
                if (rawChoice != null && !rawChoice.trim().isEmpty()) {
                    try {
                        choice = AESUtil.decrypt(rawChoice);
                    } catch (Exception e) {
                        // Fallback: if decryption fails, use raw value (might be unencrypted legacy data)
                        choice = rawChoice;
                    }
                }
                if (choice != null && !choice.trim().isEmpty()) {
                    choiceCounts.put(choice, choiceCounts.getOrDefault(choice, 0) + 1);
                }
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                itemQa.setSelectionDistributionJson(mapper.writeValueAsString(choiceCounts));
            } catch (Exception e) {
                log.error("Failed to serialize choice distribution for question {}", q.getQuestionId(), e);
            }

            // ---- 7f. 定性评价 ----
            itemQa.setDifficultyEvaluation(evaluateDifficulty(P));
            itemQa.setDiscriminationEvaluation(evaluateDiscrimination(di));

            // ---- 7g. 异常标记 ----
            itemQa.setIsTooEasy(P > 0.7);
            itemQa.setIsLowDiscrimination(di < 0.2f);
            if (itemQa.getIsTooEasy() && itemQa.getIsLowDiscrimination()) {
                itemQa.setDiagnosisTag("题目过于简单且无区分度");
            } else if (itemQa.getIsLowDiscrimination()) {
                itemQa.setDiagnosisTag("区分度较差，建议修改选项");
            } else if (P < 0.4) {
                itemQa.setDiagnosisTag("题目过难，建议降低难度");
            }

            qAnalyses.add(itemQa);
        }

        if (!qAnalyses.isEmpty()) {
            questionAnalysisService.saveBatch(qAnalyses);
        }

        // ==================== 8. 整体试卷指标 ====================
        // CTT整体难度 Q = (1/100) × Σ(满分_i × q_i)
        double overallDifficulty = calculatePaperDifficulty(questions, qAnalyses);
        // CTT整体区分度 D = (1/100) × Σ(W_i × d_i)
        double overallDiscrimination = calculatePaperDiscrimination(questions, qAnalyses);
        // CTT整体信度 R = (1/100) × Σ(W_i × r_i)
        double reliability = calculatePaperReliability(questions, qAnalyses);
        double overallValidity = K > 0 ? sumV / K : 0.0;

        // ==================== 9. 正态分布指标 (偏度 & 峰度) ====================
        double totalStdDev = Math.sqrt(totalScoreVariance);
        double skewness = calculateSkewness(scoreList, avgScore, totalStdDev, N);
        double kurtosis = calculateKurtosis(scoreList, avgScore, totalStdDev, N);

        // ==================== 10. 异常判定 ====================
        boolean isAbnormal = overallDifficulty < 0.4 || overallDifficulty > 0.7
                || overallDiscrimination < 0.2 || reliability < 0.6;

        // ==================== 11. 保存试卷分析 ====================
        deleteExistingPaperAnalysis(paperId);

        ExaminationPaperQualityAnalysis examAnalysis = new ExaminationPaperQualityAnalysis();
        examAnalysis.setPaperId(paperId);
        examAnalysis.setAverageScore(avgScore.setScale(2, RoundingMode.HALF_UP));
        examAnalysis.setStdDeviation(BigDecimal.valueOf(Math.sqrt(totalScoreVariance)).setScale(2, RoundingMode.HALF_UP));
        examAnalysis.setHighestScore(maxScore);
        examAnalysis.setLowestScore(minScore);
        examAnalysis.setReliabilityCoefficient((float) reliability);
        examAnalysis.setValidityCoefficient((float) overallValidity);
        examAnalysis.setKnowledgeCoverageRate((float) coverageRate);
        examAnalysis.setOverallDifficulty((float) overallDifficulty);
        examAnalysis.setOverallDiscrimination((float) overallDiscrimination);
        examAnalysis.setIsAbnormal(isAbnormal);
        examAnalysis.setSkewness((float) skewness);
        examAnalysis.setKurtosis((float) kurtosis);
        examAnalysis.setReliabilityEvaluation(evaluateReliability(reliability));
        examAnalysis.setDifficultyEvaluation(evaluateDifficulty(overallDifficulty));
        examAnalysis.setDiscriminationEvaluation(evaluateDiscrimination(overallDiscrimination));

        examAnalysisService.save(examAnalysis);

        // ==================== 12. 生成并保存改进建议 ====================
        generateImprovementSuggestions(paperId);

        log.info("Finished indicator computation for paperId: {}", paperId);
    }

    /**
     * 计算皮尔逊相关系数
     */
    private double calculatePearsonCorrelation(List<ComputeStudentAnswerDTO> qAns,
            Map<Long, BigDecimal> studentTotalScores,
            int totalStudents, double meanTotal) {
        // 建立该题各学生得分Map
        Map<Long, BigDecimal> itemScoreMap = new HashMap<>();
        for (ComputeStudentAnswerDTO a : qAns) {
            itemScoreMap.put(a.getStudentId(), a.getScoreObtained() != null ? a.getScoreObtained() : BigDecimal.ZERO);
        }

        // 计算该题平均分
        double sumItem = 0.0;
        for (Map.Entry<Long, BigDecimal> entry : studentTotalScores.entrySet()) {
            sumItem += itemScoreMap.getOrDefault(entry.getKey(), BigDecimal.ZERO).doubleValue();
        }
        double meanItem = totalStudents > 0 ? sumItem / totalStudents : 0.0;

        // 计算 Pearson r
        double sumXY = 0.0;
        double sumX2 = 0.0;
        double sumY2 = 0.0;

        for (Map.Entry<Long, BigDecimal> entry : studentTotalScores.entrySet()) {
            Long sid = entry.getKey();
            double xi = itemScoreMap.getOrDefault(sid, BigDecimal.ZERO).doubleValue();
            double ti = entry.getValue().doubleValue();

            double dx = xi - meanItem;
            double dy = ti - meanTotal;
            sumXY += dx * dy;
            sumX2 += dx * dx;
            sumY2 += dy * dy;
        }

        double denom = Math.sqrt(sumX2 * sumY2);
        if (denom == 0)
            return 0.0;
        return sumXY / denom;
    }

    /**
     * 计算偏度
     */
    private double calculateSkewness(List<BigDecimal> data, BigDecimal mean, double stdDev, int n) {
        if (n < 3 || stdDev == 0)
            return 0.0;
        double sum = 0.0;
        double meanVal = mean.doubleValue();
        for (BigDecimal val : data) {
            double z = (val.doubleValue() - meanVal) / stdDev;
            sum += z * z * z;
        }
        return ((double) n / ((n - 1) * (n - 2))) * sum;
    }

    /**
     * 计算峰度
     */
    private double calculateKurtosis(List<BigDecimal> data, BigDecimal mean, double stdDev, int n) {
        if (n < 4 || stdDev == 0)
            return 0.0;
        double sum = 0.0;
        double meanVal = mean.doubleValue();
        for (BigDecimal val : data) {
            double z = (val.doubleValue() - meanVal) / stdDev;
            sum += z * z * z * z;
        }
        double term1 = ((double) n * (n + 1)) / ((double) (n - 1) * (n - 2) * (n - 3));
        double term2 = (3.0 * (n - 1) * (n - 1)) / ((double) (n - 2) * (n - 3));
        return term1 * sum - term2;
    }

    // ==================== 定性评价映射 ====================

    /** 信度评价: α >= 0.8 → 高, α >= 0.6 → 中, α < 0.6 → 低 */
    private String evaluateReliability(double alpha) {
        if (alpha >= 0.8)
            return "高";
        if (alpha >= 0.6)
            return "中";
        return "低";
    }

    /** 难度评价: P > 0.7 → 偏易, P >= 0.4 → 适中, P < 0.4 → 偏难 */
    private String evaluateDifficulty(double p) {
        if (p > 0.7)
            return "偏易";
        if (p >= 0.4)
            return "适中";
        return "偏难";
    }

    /**
     * 区分度评价: D >= 0.4 → 优秀, D >= 0.3 → 良好, D >= 0.2 → 一般, D < 0.2 → 差
     */
    private String evaluateDiscrimination(double d) {
        if (d >= 0.4)
            return "优秀";
        if (d >= 0.3)
            return "良好";
        if (d >= 0.2)
            return "一般";
        return "差";
    }

    // ==================== 方差计算 (CTT) ====================

    /**
     * 计算样本方差 (基于经典测量理论 CTT)
     * 公式: S² = (1 / (N-1)) × Σ(X_j - Mean)²
     * 当 N ≤ 1 时返回 0.0，避免除零异常
     *
     * @param data 数据列表
     * @param mean 平均值
     * @return 样本方差 S²
     */
    private double calculateVariance(List<BigDecimal> data, BigDecimal mean) {
        if (data == null || data.size() <= 1) {
            return 0.0;
        }
        double meanVal = mean.doubleValue();
        double sumSquareDiffs = 0.0;
        for (BigDecimal val : data) {
            double diff = val.doubleValue() - meanVal;
            sumSquareDiffs += diff * diff;
        }
        return sumSquareDiffs / (data.size() - 1);
    }

    // ==================== CTT 难度计算方法 ====================

    /**
     * 计算单题难度 (基于经典测量理论 CTT)
     * 公式: q_i = 1 - (该题平均得分 / 该题满分)
     * 难度范围 [0, 1]，值越大表示题目越难
     *
     * @param qAns           该题所有学生的答题记录
     * @param fullScore      该题满分
     * @param totalStudents  有效样本数量 N
     * @return 单题难度系数 q_i
     */
    private float calculateQuestionDifficulty(List<ComputeStudentAnswerDTO> qAns,
                                              BigDecimal fullScore, int totalStudents) {
        // 边界保护：无学生作答或满分为0时，返回默认难度
        if (qAns == null || qAns.isEmpty() || totalStudents <= 0) {
            return 0.5f; // 默认中等难度
        }
        if (fullScore == null || fullScore.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.5f; // 满分为0时无法计算，返回默认值
        }

        // 计算该题所有学生的平均得分
        BigDecimal sumScores = BigDecimal.ZERO;
        for (ComputeStudentAnswerDTO a : qAns) {
            BigDecimal score = a.getScoreObtained() != null ? a.getScoreObtained() : BigDecimal.ZERO;
            sumScores = sumScores.add(score);
        }
        // 注意：部分学生可能未作答（不在 qAns 中），仍以 totalStudents 为分母
        BigDecimal avgScore = sumScores.divide(BigDecimal.valueOf(totalStudents), 6, RoundingMode.HALF_UP);

        // q_i = 1 - (平均得分 / 满分)
        double qi = 1.0 - avgScore.divide(fullScore, 6, RoundingMode.HALF_UP).doubleValue();

        // 限定在 [0, 1] 范围内
        qi = Math.max(0.0, Math.min(1.0, qi));
        return (float) qi;
    }

    /**
     * 计算整体试卷难度 (基于经典测量理论 CTT)
     * 公式: Q = (1/100) × Σ(满分_i × q_i)
     * 其中 q_i 为每道题目的 CTT 难度系数
     *
     * @param paperQuestions 试卷题目列表（含满分信息）
     * @param qAnalyses      各题已计算的质量分析结果（含 q_i）
     * @return 整体试卷难度 Q
     */
    private double calculatePaperDifficulty(List<ComputePaperQuestionDTO> paperQuestions,
                                            List<QuestionQualityAnalysis> qAnalyses) {
        if (paperQuestions == null || paperQuestions.isEmpty() || qAnalyses == null || qAnalyses.isEmpty()) {
            return 0.0;
        }

        // 建立 questionId -> q_i 的映射，方便查找
        Map<Long, Float> difficultyMap = new HashMap<>();
        for (QuestionQualityAnalysis qa : qAnalyses) {
            // correctResponseRate 字段此时存储的是 CTT 难度 q_i
            difficultyMap.put(qa.getQuestionId(), qa.getCorrectResponseRate());
        }

        // Q = (1/100) × Σ(满分_i × q_i)
        double weightedSum = 0.0;
        for (ComputePaperQuestionDTO q : paperQuestions) {
            BigDecimal fullScore = q.getScoreValue();
            if (fullScore == null || fullScore.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // 跳过满分无效的题目
            }
            Float qi = difficultyMap.get(q.getQuestionId());
            if (qi == null) {
                continue; // 跳过没有分析结果的题目
            }
            weightedSum += fullScore.doubleValue() * qi;
        }

        double Q = weightedSum / 100.0;
        // 限定在 [0, 1] 范围内
        return Math.max(0.0, Math.min(1.0, Q));
    }

    // ==================== CTT 信度计算方法 ====================

    /**
     * 计算单题信度系数 r_i (Cronbach's Alpha)
     * 公式: r_i = (K / (K-1)) × (1 - Σ(S_i²) / S_X²)
     * K:    试卷总题数
     * S_i²: 第 i 题得分的方差
     * S_X²: 所有学生总分的方差
     *
     * @param questions          试卷题目列表
     * @param answersByQuestion  按题目ID分组的学生答题记录
     * @param totalStudents      有效学生数量 N
     * @param totalScoreVariance 总分方差 S_X²
     * @return 单题信度系数 r_i
     */
    private float calculateQuestionReliability(List<ComputePaperQuestionDTO> questions,
                                                Map<Long, List<ComputeStudentAnswerDTO>> answersByQuestion,
                                                int totalStudents,
                                                double totalScoreVariance) {
        int K = questions.size();

        // 防御性编程：题数不足或总分方差为0时无法计算信度
        if (K <= 1 || totalScoreVariance <= 0.0 || totalStudents <= 1) {
            return 0.0f;
        }

        // 计算各题得分方差之和 Σ(S_i²)
        double sumItemVariances = 0.0;
        for (ComputePaperQuestionDTO q : questions) {
            List<ComputeStudentAnswerDTO> qAnswers = answersByQuestion.getOrDefault(
                    q.getQuestionId(), new ArrayList<>());

            // 收集该题所有学生的得分
            List<BigDecimal> itemScores = qAnswers.stream()
                    .map(a -> a.getScoreObtained() != null ? a.getScoreObtained() : BigDecimal.ZERO)
                    .collect(Collectors.toList());

            // 填充缺失的答题记录为0分（未作答的学生视为0分）
            while (itemScores.size() < totalStudents) {
                itemScores.add(BigDecimal.ZERO);
            }

            // 计算该题得分的平均值和方差 S_i²
            BigDecimal sumItem = itemScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgItem = sumItem.divide(BigDecimal.valueOf(totalStudents), 4, RoundingMode.HALF_UP);
            double itemVariance = calculateVariance(itemScores, avgItem);
            sumItemVariances += itemVariance;
        }

        // r_i = (K / (K-1)) × (1 - Σ(S_i²) / S_X²)
        double ri = ((double) K / (K - 1)) * (1.0 - (sumItemVariances / totalScoreVariance));

        // 限定在 [0, 1] 范围内
        ri = Math.max(0.0, Math.min(1.0, ri));
        return (float) ri;
    }

    /**
     * 计算整体试卷信度 (基于经典测量理论 CTT)
     * 公式: R = (1/100) × Σ(W_i × r_i)
     * 其中 W_i 为每道题的满分，r_i 为每道题的信度系数
     *
     * @param paperQuestions 试卷题目列表（含满分信息）
     * @param qAnalyses      各题已计算的质量分析结果（含 r_i）
     * @return 整体试卷信度 R
     */
    private double calculatePaperReliability(List<ComputePaperQuestionDTO> paperQuestions,
                                              List<QuestionQualityAnalysis> qAnalyses) {
        if (paperQuestions == null || paperQuestions.isEmpty() || qAnalyses == null || qAnalyses.isEmpty()) {
            return 0.0;
        }

        // 建立 questionId -> r_i 的映射
        Map<Long, Float> reliabilityMap = new HashMap<>();
        for (QuestionQualityAnalysis qa : qAnalyses) {
            if (qa.getReliabilityCoefficient() != null) {
                reliabilityMap.put(qa.getQuestionId(), qa.getReliabilityCoefficient());
            }
        }

        // R = (1/100) × Σ(W_i × r_i)
        double weightedSum = 0.0;
        for (ComputePaperQuestionDTO q : paperQuestions) {
            BigDecimal fullScore = q.getScoreValue();
            if (fullScore == null || fullScore.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // 跳过满分无效的题目
            }
            Float ri = reliabilityMap.get(q.getQuestionId());
            if (ri == null) {
                continue; // 跳过没有信度结果的题目
            }
            weightedSum += fullScore.doubleValue() * ri;
        }

        double R = weightedSum / 100.0;
        // 限定在 [0, 1] 范围内
        return Math.max(0.0, Math.min(1.0, R));
    }

    // ==================== CTT 区分度计算方法 ====================

    /**
     * 计算单题区分度 (基于经典测量理论 CTT)
     * 公式: d_i = (H_i - L_i) / W_i
     * H_i: 高分组在该题的平均得分
     * L_i: 低分组在该题的平均得分
     * W_i: 该题满分
     *
     * @param qAns          该题所有学生的答题记录
     * @param highGroupIds  高分组学生 ID 集合
     * @param lowGroupIds   低分组学生 ID 集合
     * @param fullScore     该题满分 W_i
     * @return 单题区分度系数 d_i
     */
    private float calculateQuestionDiscrimination(List<ComputeStudentAnswerDTO> qAns,
                                                   Set<Long> highGroupIds,
                                                   Set<Long> lowGroupIds,
                                                   BigDecimal fullScore) {
        // 防御性编程：满分为0时无法计算区分度
        if (fullScore == null || fullScore.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0f;
        }

        // 统计高分组和低分组在该题的得分总和及人数
        BigDecimal highSum = BigDecimal.ZERO;
        int highCount = 0;
        BigDecimal lowSum = BigDecimal.ZERO;
        int lowCount = 0;

        for (ComputeStudentAnswerDTO a : qAns) {
            BigDecimal score = a.getScoreObtained() != null ? a.getScoreObtained() : BigDecimal.ZERO;
            if (highGroupIds.contains(a.getStudentId())) {
                highSum = highSum.add(score);
                highCount++;
            }
            if (lowGroupIds.contains(a.getStudentId())) {
                lowSum = lowSum.add(score);
                lowCount++;
            }
        }

        // 防御性编程：某组无样本时设该组平均分为0
        double Hi = highCount > 0
                ? highSum.divide(BigDecimal.valueOf(highCount), 6, RoundingMode.HALF_UP).doubleValue()
                : 0.0;
        double Li = lowCount > 0
                ? lowSum.divide(BigDecimal.valueOf(lowCount), 6, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // d_i = (H_i - L_i) / W_i
        double di = (Hi - Li) / fullScore.doubleValue();

        // 限定在 [-1, 1] 范围内
        di = Math.max(-1.0, Math.min(1.0, di));
        return (float) di;
    }

    /**
     * 计算整体试卷区分度 (基于经典测量理论 CTT)
     * 公式: D = (1/100) × Σ(W_i × d_i)
     * 其中 W_i 为每道题的满分，d_i 为每道题的区分度
     *
     * @param paperQuestions 试卷题目列表（含满分信息）
     * @param qAnalyses      各题已计算的质量分析结果（含 d_i）
     * @return 整体试卷区分度 D
     */
    private double calculatePaperDiscrimination(List<ComputePaperQuestionDTO> paperQuestions,
                                                 List<QuestionQualityAnalysis> qAnalyses) {
        if (paperQuestions == null || paperQuestions.isEmpty() || qAnalyses == null || qAnalyses.isEmpty()) {
            return 0.0;
        }

        // 建立 questionId -> d_i 的映射
        Map<Long, Float> discriminationMap = new HashMap<>();
        for (QuestionQualityAnalysis qa : qAnalyses) {
            discriminationMap.put(qa.getQuestionId(), qa.getDiscriminationIndex());
        }

        // D = (1/100) × Σ(W_i × d_i)
        double weightedSum = 0.0;
        for (ComputePaperQuestionDTO q : paperQuestions) {
            BigDecimal fullScore = q.getScoreValue();
            if (fullScore == null || fullScore.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // 跳过满分无效的题目
            }
            Float di = discriminationMap.get(q.getQuestionId());
            if (di == null) {
                continue; // 跳过没有分析结果的题目
            }
            weightedSum += fullScore.doubleValue() * di;
        }

        double D = weightedSum / 100.0;
        // 限定在 [-1, 1] 范围内
        return Math.max(-1.0, Math.min(1.0, D));
    }

    // ==================== 改进建议生成 ====================
    private void generateImprovementSuggestions(Long paperId) {
        computeMapper.deleteSuggestionsByPaperId(paperId);
        List<QuestionAnalysisDTO> dtos = computeMapper.getQuestionAnalysisDetailsByPaperId(paperId);
        ObjectMapper mapper = new ObjectMapper();

        for (QuestionAnalysisDTO dto : dtos) {
            // 规则1：难度检查 (使用difficultyIndex)
            Float diffIdx = dto.getDifficultyIndex();
            if (diffIdx != null) {
                if (diffIdx < 0.4f) {
                    saveSuggestion(paperId, dto.getQuestionId(), "Difficulty_Adj",
                            "题目难度过高 (P=" + String.format("%.2f", diffIdx) + ", " + evaluateDifficulty(diffIdx)
                                    + ")，建议修改题干或干扰项。");

                    if (dto.getStem() != null && (dto.getStem().toUpperCase().contains("NOT") ||
                            dto.getStem().toUpperCase().contains("EXCEPT") ||
                            dto.getStem().contains("不") || dto.getStem().contains("除了"))) {
                        saveSuggestion(paperId, dto.getQuestionId(), "Question_Content",
                                "这是一道难度较高的否定形式题目。建议在题干中加粗或高亮否定词。");
                    }
                } else if (diffIdx > 0.7f) {
                    saveSuggestion(paperId, dto.getQuestionId(), "Difficulty_Adj",
                            "题目过于简单 (P=" + String.format("%.2f", diffIdx) + ", " + evaluateDifficulty(diffIdx)
                                    + ")，请检查正确答案是否过于明显。");
                }
            }

            // 规则2：区分度检查
            if (dto.getDiscriminationIndex() != null && dto.getDiscriminationIndex() < 0.2f) {
                saveSuggestion(paperId, dto.getQuestionId(), "Question_Content",
                        "区分度较低 (D=" + String.format("%.2f", dto.getDiscriminationIndex()) + ", "
                                + evaluateDiscrimination(dto.getDiscriminationIndex()) + ")。建议优化选项。");
            }

            // 解析选项JSON进行详细检查
            Map<String, String> optionsMap = null;
            if (dto.getOptionsJson() != null && !dto.getOptionsJson().isEmpty() &&
                    ("SingleChoice".equals(dto.getQuestionType()) || "MultipleChoice".equals(dto.getQuestionType()))) {
                try {
                    optionsMap = mapper.readValue(dto.getOptionsJson(), new TypeReference<Map<String, String>>() {
                    });
                } catch (Exception e) {
                }
            }

            // 规则3：选项分布
            if (dto.getSelectionDistributionJson() != null && !dto.getSelectionDistributionJson().isEmpty() &&
                    "SingleChoice".equals(dto.getQuestionType())) {
                try {
                    Map<String, Integer> distribution = mapper.readValue(dto.getSelectionDistributionJson(),
                            new TypeReference<Map<String, Integer>>() {
                            });
                    String[] standardOptions = { "A", "B", "C", "D" };
                    for (String opt : standardOptions) {
                        if (!distribution.containsKey(opt) || distribution.get(opt) == 0) {
                            String optText = (optionsMap != null && optionsMap.containsKey(opt))
                                    ? " ('" + optionsMap.get(opt) + "')"
                                    : "";
                            saveSuggestion(paperId, dto.getQuestionId(), "Question_Content",
                                    "选项 " + opt + optText + " 极少或从未被选过。建议将其替换为更有迷惑性的干扰项。");
                        }
                    }
                } catch (Exception e) {
                }
            }

            // 规则4：长度偏差检查
            if (optionsMap != null && dto.getCorrectAnswer() != null && "SingleChoice".equals(dto.getQuestionType())) {
                String correctOptKey = dto.getCorrectAnswer().trim();
                String correctText = optionsMap.get(correctOptKey);
                if (correctText != null) {
                    int correctLen = correctText.length();
                    boolean isSignificantlyLonger = true;
                    int validDistractors = 0;
                    for (Map.Entry<String, String> entry : optionsMap.entrySet()) {
                        if (!entry.getKey().equals(correctOptKey) && entry.getValue() != null) {
                            validDistractors++;
                            if (correctLen <= entry.getValue().length() * 1.5) {
                                isSignificantlyLonger = false;
                                break;
                            }
                        }
                    }
                    if (validDistractors > 0 && isSignificantlyLonger) {
                        saveSuggestion(paperId, dto.getQuestionId(), "Question_Content",
                                "潜在长度偏差：正确答案 (" + correctOptKey + ") 明显长于其他干扰项。学生可能会根据长度猜测答案。");
                    }
                }
            }
        }
    }

    private void saveSuggestion(Long paperId, Long questionId, String type, String text) {
        computeMapper.insertImprovementSuggestion(paperId, questionId, type, text, text, false);
    }

    private void deleteExistingPaperAnalysis(Long paperId) {
        computeMapper.deletePaperQualityAnalysisByPaperId(paperId);
    }

    private void deleteExistingItemAnalysis(Long paperId) {
        computeMapper.deleteQuestionQualityAnalysisByPaperId(paperId);
    }
}
