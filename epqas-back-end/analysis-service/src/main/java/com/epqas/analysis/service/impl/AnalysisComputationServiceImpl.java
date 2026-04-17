package com.epqas.analysis.service.impl;

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
     * 计算考试指标
     * 
     * @param examId 考试ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateExamIndicators(Long examId) {
        log.info("Starting indicator computation for examId: {}", examId);

        // ==================== 1. 加载上下文数据 ====================
        List<ComputeExamResultDTO> results = computeMapper.selectExamResults(examId);
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("没有找到学生成绩数据，无法进行计算");
        }

        List<ComputeStudentAnswerDTO> answers = computeMapper.selectStudentAnswers(examId);
        List<ComputePaperQuestionDTO> questions = computeMapper.selectPaperQuestions(examId);

        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("没有找到试卷题目数据，无法进行计算");
        }

        // ==================== 2. 基础统计 ====================
        Long paperId = questions.get(0).getPaperId();
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
        double totalStdDev = calculateStdDev(scoreList, avgScore); // σ_T

        // ==================== 3. 极端组分组 (25%) ====================
        results.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore())); // 降序
        int groupSize = (int) Math.floor(N * 0.25); // 取25%，向下取整
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

        // ==================== 6. 信度 Cronbach's Alpha ====================
        // α = (K / K-1) * (1 - Σ(S_i²) / S_T²)
        int K = questions.size();
        double sumItemVariances = 0.0;
        double reliability = 0.0;

        if (K > 1 && totalStdDev > 0) {
            for (ComputePaperQuestionDTO q : questions) {
                List<ComputeStudentAnswerDTO> qAnswers = answersByQuestion.getOrDefault(q.getQuestionId(),
                        new ArrayList<>());
                List<BigDecimal> itemScores = qAnswers.stream()
                        .map(a -> a.getScoreObtained() != null ? a.getScoreObtained() : BigDecimal.ZERO)
                        .collect(Collectors.toList());
                // 填充缺失的答题记录为0分
                while (itemScores.size() < N) {
                    itemScores.add(BigDecimal.ZERO);
                }
                BigDecimal sumItem = itemScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avgItem = sumItem.divide(BigDecimal.valueOf(N), 4, RoundingMode.HALF_UP);
                double itemVar = Math.pow(calculateStdDev(itemScores, avgItem), 2);
                sumItemVariances += itemVar;
            }
            reliability = ((double) K / (K - 1)) * (1.0 - (sumItemVariances / Math.pow(totalStdDev, 2)));
            reliability = Math.max(0.0, Math.min(1.0, reliability));
        }

        // ==================== 7. 逐题指标计算 (P, D, V) ====================
        deleteExistingItemAnalysis(examId);
        List<QuestionQualityAnalysis> qAnalyses = new ArrayList<>();
        double sumP = 0.0; // 用于计算平均难度
        double sumD = 0.0; // 用于计算平均区分度
        double sumV = 0.0; // 用于计算平均效度

        for (ComputePaperQuestionDTO q : questions) {
            QuestionQualityAnalysis itemQa = new QuestionQualityAnalysis();
            itemQa.setExamId(examId);
            itemQa.setQuestionId(q.getQuestionId());

            List<ComputeStudentAnswerDTO> qAns = answersByQuestion.getOrDefault(q.getQuestionId(), new ArrayList<>());
            BigDecimal H = q.getScoreValue(); // 该题满分
            if (H == null || H.compareTo(BigDecimal.ZERO) == 0)
                H = BigDecimal.ONE;

            // ---- 7a. 正确反应率 (保留向后兼容) ----
            long correctCount = qAns.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
            float correctRate = N > 0 ? (float) correctCount / N : 0f;
            itemQa.setCorrectResponseRate(correctRate);

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
            sumP += P;

            // ---- 7c. 极端组法区分度 D = (X_H - X_L) / (groupSize * H) ----
            double denomD = groupSize * H.doubleValue();
            double D = denomD > 0 ? (xH.doubleValue() - xL.doubleValue()) / denomD : 0.0;
            D = Math.max(-1.0, Math.min(1.0, D));
            itemQa.setDiscriminationIndex((float) D);
            sumD += D;

            // ---- 7d. 效度 Pearson r(i,T) ----
            // r = Σ((Xi - X̄i)(T - T̄)) / sqrt(Σ(Xi - X̄i)² * Σ(T - T̄)²)
            double validity = calculatePearsonCorrelation(qAns, studentTotalScores, N, meanTotal);
            itemQa.setValidityIndex((float) validity);
            sumV += Math.abs(validity);

            // ---- 7e. 选项分布 ----
            Map<String, Integer> choiceCounts = new HashMap<>();
            for (ComputeStudentAnswerDTO a : qAns) {
                String choice = a.getStudentChoice();
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
            itemQa.setDiscriminationEvaluation(evaluateDiscrimination(D));

            // ---- 7g. 异常标记 ----
            itemQa.setIsTooEasy(P > 0.7);
            itemQa.setIsLowDiscrimination(D < 0.2);
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
        double overallDifficulty = K > 0 ? sumP / K : 0.0;
        double overallDiscrimination = K > 0 ? sumD / K : 0.0;
        double overallValidity = K > 0 ? sumV / K : 0.0;

        // ==================== 9. 正态分布指标 (偏度 & 峰度) ====================
        double skewness = calculateSkewness(scoreList, avgScore, totalStdDev, N);
        double kurtosis = calculateKurtosis(scoreList, avgScore, totalStdDev, N);

        // ==================== 10. 异常判定 ====================
        boolean isAbnormal = overallDifficulty < 0.4 || overallDifficulty > 0.7
                || overallDiscrimination < 0.2 || reliability < 0.6;

        // ==================== 11. 保存试卷分析 ====================
        deleteExistingPaperAnalysis(examId);

        ExaminationPaperQualityAnalysis examAnalysis = new ExaminationPaperQualityAnalysis();
        examAnalysis.setExamId(examId);
        examAnalysis.setAverageScore(avgScore.setScale(2, RoundingMode.HALF_UP));
        examAnalysis.setStdDeviation(BigDecimal.valueOf(totalStdDev).setScale(2, RoundingMode.HALF_UP));
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
        generateImprovementSuggestions(examId);

        log.info("Finished indicator computation for examId: {}", examId);
    }

    /**
     * 计算皮尔逊相关系数
     * 
     * @param qAns               题目答案
     * @param studentTotalScores 学生总分
     * @param totalStudents      学生总数
     * @param meanTotal          平均总分
     * @return 皮尔逊相关系数
     */
    // ==================== Pearson相关系数 r(i,T) ====================
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
     * 
     * @param data   数据
     * @param mean   平均值
     * @param stdDev 标准差
     * @param n      数据数量
     * @return 偏度
     */
    // ==================== 偏度 (Skewness) ====================
    // Skewness = [n / ((n-1)(n-2))] * Σ((xi - x̄) / σ)³
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
     * 
     * @param data   数据
     * @param mean   平均值
     * @param stdDev 标准差
     * @param n      数据数量
     * @return 峰度
     */
    // ==================== 峰度 (Excess Kurtosis) ====================
    // Kurtosis = [n(n+1) / ((n-1)(n-2)(n-3))] * Σ((xi - x̄) / σ)⁴ - [3(n-1)² /
    // ((n-2)(n-3))]
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

    /**
     * 评价信度
     * 
     * @param alpha 信度
     * @return 评价结果
     */
    /** 信度评价: α >= 0.8 → 高, α >= 0.6 → 中, α < 0.6 → 低 */
    private String evaluateReliability(double alpha) {
        if (alpha >= 0.8)
            return "高";
        if (alpha >= 0.6)
            return "中";
        return "低";
    }

    /**
     * 评价难度
     * 
     * @param p 难度
     * @return 评价结果
     */
    /** 难度评价: P > 0.7 → 偏易, P >= 0.4 → 适中, P < 0.4 → 偏难 */
    private String evaluateDifficulty(double p) {
        if (p > 0.7)
            return "偏易";
        if (p >= 0.4)
            return "适中";
        return "偏难";
    }

    /**
     * 评价区分度
     * 
     * @param d 区分度
     * @return 评价结果
     */
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

    /**
     * 计算标准差
     * 
     * @param data 数据
     * @param mean 平均值
     * @return 标准差
     */
    // ==================== 标准差 (总体) ====================
    private double calculateStdDev(List<BigDecimal> data, BigDecimal mean) {
        if (data.size() <= 1)
            return 0.0;
        double sumSquareDiffs = 0.0;
        for (BigDecimal val : data) {
            double diff = val.doubleValue() - mean.doubleValue();
            sumSquareDiffs += diff * diff;
        }
        return Math.sqrt(sumSquareDiffs / data.size());
    }

    /**
     * 生成改进建议
     * 
     * @param examId 考试ID
     */
    // ==================== 改进建议生成 ====================
    private void generateImprovementSuggestions(Long examId) {
        computeMapper.deleteSuggestionsByExamId(examId);
        List<QuestionAnalysisDTO> dtos = computeMapper.getQuestionAnalysisDetailsByExamId(examId);
        ObjectMapper mapper = new ObjectMapper();

        for (QuestionAnalysisDTO dto : dtos) {
            // 规则1：难度检查 (使用difficultyIndex)
            Float diffIdx = dto.getDifficultyIndex();
            if (diffIdx != null) {
                if (diffIdx < 0.4f) {
                    saveSuggestion(examId, dto.getQuestionId(), "Difficulty_Adj",
                            "题目难度过高 (P=" + String.format("%.2f", diffIdx) + ", " + evaluateDifficulty(diffIdx)
                                    + ")，建议修改题干或干扰项。");

                    if (dto.getStem() != null && (dto.getStem().toUpperCase().contains("NOT") ||
                            dto.getStem().toUpperCase().contains("EXCEPT") ||
                            dto.getStem().contains("不") || dto.getStem().contains("除了"))) {
                        saveSuggestion(examId, dto.getQuestionId(), "Question_Content",
                                "这是一道难度较高的否定形式题目。建议在题干中加粗或高亮否定词。");
                    }
                } else if (diffIdx > 0.7f) {
                    saveSuggestion(examId, dto.getQuestionId(), "Difficulty_Adj",
                            "题目过于简单 (P=" + String.format("%.2f", diffIdx) + ", " + evaluateDifficulty(diffIdx)
                                    + ")，请检查正确答案是否过于明显。");
                }
            }

            // 规则2：区分度检查
            if (dto.getDiscriminationIndex() != null && dto.getDiscriminationIndex() < 0.2f) {
                saveSuggestion(examId, dto.getQuestionId(), "Question_Content",
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
                            saveSuggestion(examId, dto.getQuestionId(), "Question_Content",
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
                        saveSuggestion(examId, dto.getQuestionId(), "Question_Content",
                                "潜在长度偏差：正确答案 (" + correctOptKey + ") 明显长于其他干扰项。学生可能会根据长度猜测答案。");
                    }
                }
            }
        }
    }

    /**
     * 保存改进建议
     * 
     * @param examId     考试ID
     * @param questionId 题目ID
     * @param type       建议类型
     * @param text       建议内容
     */
    private void saveSuggestion(Long examId, Long questionId, String type, String text) {
        computeMapper.insertImprovementSuggestion(examId, questionId, type, text, text, false);
    }

    /**
     * 删除已存在的试卷分析
     * 
     * @param examId 考试ID
     */
    private void deleteExistingPaperAnalysis(Long examId) {
        computeMapper.deletePaperQualityAnalysisByExamId(examId);
    }

    /**
     * 删除已存在的题目分析
     * 
     * @param examId 考试ID
     */
    private void deleteExistingItemAnalysis(Long examId) {
        computeMapper.deleteQuestionQualityAnalysisByExamId(examId);
    }
}
