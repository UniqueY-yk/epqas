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
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateExamIndicators(Long examId) {
        log.info("Starting indicator computation for examId: {}", examId);

        // 1. Load context data
        List<ComputeExamResultDTO> results = computeMapper.selectExamResults(examId);
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("No student results found for calculation.");
        }

        List<ComputeStudentAnswerDTO> answers = computeMapper.selectStudentAnswers(examId);
        List<ComputePaperQuestionDTO> questions = computeMapper.selectPaperQuestions(examId);

        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("No questions configured for this exam paper.");
        }

        // 2. Fetch total paper score & context
        BigDecimal totalExamScore = questions.stream()
                .map(ComputePaperQuestionDTO::getScoreValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Fetch course and paper context manually given standard foreign keys
        // (Assuming exams map back properly, fallback courseId lookup)
        Long paperId = questions.get(0).getPaperId();
        Integer courseId = jdbcTemplate.queryForObject(
                "SELECT course_id FROM examination_paper WHERE paper_id = ?",
                Integer.class, paperId);

        // 3. Compute Basic Exam Statistics (Average, Max, Min, StdDev)
        BigDecimal sumScores = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal minScore = new BigDecimal("9999");

        List<BigDecimal> scoreList = new ArrayList<>();
        for (ComputeExamResultDTO r : results) {
            BigDecimal ts = r.getTotalScore();
            if (ts == null)
                ts = BigDecimal.ZERO;
            scoreList.add(ts);
            sumScores = sumScores.add(ts);
            if (ts.compareTo(maxScore) > 0)
                maxScore = ts;
            if (ts.compareTo(minScore) < 0)
                minScore = ts;
        }

        int N = results.size();
        BigDecimal avgScore = sumScores.divide(BigDecimal.valueOf(N), 2, RoundingMode.HALF_UP);
        double stdDev = calculateStdDev(scoreList, avgScore);

        // 4. Compute Overall Difficulty (μ / Max_Score)
        double overallDifficulty = 0.0;
        if (totalExamScore.compareTo(BigDecimal.ZERO) > 0) {
            overallDifficulty = avgScore.doubleValue() / totalExamScore.doubleValue();
        }

        // 5. Compute Knowledge Coverage Rate
        Integer totalCoursePoints = computeMapper.countCourseKnowledgePoints(courseId);
        Integer paperPoints = computeMapper.countPaperKnowledgePoints(paperId);
        double coverageRate = 0.0;
        if (totalCoursePoints != null && totalCoursePoints > 0 && paperPoints != null) {
            coverageRate = (double) paperPoints / totalCoursePoints;
        }

        // 6. Compute Discrimination Index
        // Sort students by score to find Top 27% and Bottom 27%
        results.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore())); // Descending
        int topBottomCount = (int) Math.max(1, Math.round(N * 0.27));

        List<ComputeExamResultDTO> highGroup = results.subList(0, Math.min(topBottomCount, N));
        List<ComputeExamResultDTO> lowGroup = results.subList(Math.max(0, N - topBottomCount), N);

        double highGroupAvg = highGroup.stream()
                .map(ComputeExamResultDTO::getTotalScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(highGroup.size()), 2, RoundingMode.HALF_UP)
                .doubleValue();

        double lowGroupAvg = lowGroup.stream()
                .map(ComputeExamResultDTO::getTotalScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(lowGroup.size()), 2, RoundingMode.HALF_UP)
                .doubleValue();

        double overallDiscrimination = 0.0;
        if (totalExamScore.compareTo(BigDecimal.ZERO) > 0) {
            overallDiscrimination = (highGroupAvg - lowGroupAvg) / totalExamScore.doubleValue();
        }

        // 7. Compute Reliability (Cronbach's Alpha)
        // ά = (K / K-1) * (1 - sum(σ_i^2) / σ_x^2)
        int K = questions.size();
        double sumItemVariances = 0.0;

        if (K > 1 && stdDev > 0) {
            Map<Long, List<ComputeStudentAnswerDTO>> answersByQuestion = answers.stream()
                    .collect(Collectors.groupingBy(ComputeStudentAnswerDTO::getQuestionId));

            for (ComputePaperQuestionDTO q : questions) {
                List<ComputeStudentAnswerDTO> qAnswers = answersByQuestion.getOrDefault(q.getQuestionId(),
                        new ArrayList<>());
                List<BigDecimal> itemScores = qAnswers.stream()
                        .map(a -> a.getScoreObtained() != null ? a.getScoreObtained() : BigDecimal.ZERO)
                        .collect(Collectors.toList());

                // Pad missing answers with zero score
                while (itemScores.size() < N) {
                    itemScores.add(BigDecimal.ZERO);
                }

                BigDecimal sumItemScore = itemScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avgItemScore = sumItemScore.divide(BigDecimal.valueOf(N), 2, RoundingMode.HALF_UP);
                double itemVar = Math.pow(calculateStdDev(itemScores, avgItemScore), 2);
                sumItemVariances += itemVar;
            }

            double reliability = ((double) K / (K - 1)) * (1.0 - (sumItemVariances / Math.pow(stdDev, 2)));
            // Clamp
            reliability = Math.max(0.0, Math.min(1.0, reliability));
        }

        double validity = 0.85; // Heuristic placeholder

        // Check if anything is abnormal (e.g., extremely hard or extremely low
        // discrimination)
        boolean isAbnormal = overallDifficulty < 0.3 || overallDiscrimination < 0.2;

        // 8. Save Paper Analysis
        deleteExistingPaperAnalysis(examId);

        ExaminationPaperQualityAnalysis examAnalysis = new ExaminationPaperQualityAnalysis();
        examAnalysis.setExamId(examId);
        examAnalysis.setAverageScore(avgScore);
        examAnalysis.setStdDeviation(BigDecimal.valueOf(stdDev));
        examAnalysis.setHighestScore(maxScore);
        examAnalysis.setLowestScore(minScore);
        examAnalysis.setReliabilityCoefficient((float) overallDiscrimination); // FIX: Storing Cronbach later, reuse
                                                                               // variable
        examAnalysis.setReliabilityCoefficient(
                (float) (K > 1 ? ((double) K / (K - 1)) * (1.0 - (sumItemVariances / Math.pow(stdDev, 2))) : 0.8));
        examAnalysis.setValidityCoefficient((float) validity);
        examAnalysis.setKnowledgeCoverageRate((float) coverageRate);
        examAnalysis.setOverallDifficulty((float) overallDifficulty);
        examAnalysis.setOverallDiscrimination((float) overallDiscrimination);
        examAnalysis.setIsAbnormal(isAbnormal);

        examAnalysisService.save(examAnalysis);

        // 9. Compute & Save Item-Level Metrics
        deleteExistingItemAnalysis(examId);
        List<QuestionQualityAnalysis> qAnalyses = new ArrayList<>();

        Set<Long> highGroupIds = highGroup.stream().map(ComputeExamResultDTO::getStudentId).collect(Collectors.toSet());
        Set<Long> lowGroupIds = lowGroup.stream().map(ComputeExamResultDTO::getStudentId).collect(Collectors.toSet());

        for (ComputePaperQuestionDTO q : questions) {
            QuestionQualityAnalysis itemQa = new QuestionQualityAnalysis();
            itemQa.setExamId(examId);
            itemQa.setQuestionId(q.getQuestionId());

            List<ComputeStudentAnswerDTO> qAns = answers.stream()
                    .filter(a -> a.getQuestionId().equals(q.getQuestionId()))
                    .collect(Collectors.toList());

            // Correct Response Rate (P-Value / Difficulty)
            long correctCount = qAns.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
            float rRate = N > 0 ? (float) correctCount / N : 0f;
            itemQa.setCorrectResponseRate(rRate);

            // Compute Selection Distribution
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

            // Item Discrimination: (CH - CL) / (N/2 roughly) based on groups
            long highCorrect = qAns.stream()
                    .filter(a -> highGroupIds.contains(a.getStudentId()) && Boolean.TRUE.equals(a.getIsCorrect()))
                    .count();
            long lowCorrect = qAns.stream()
                    .filter(a -> lowGroupIds.contains(a.getStudentId()) && Boolean.TRUE.equals(a.getIsCorrect()))
                    .count();

            float itemDisc = topBottomCount > 0 ? (float) (highCorrect - lowCorrect) / topBottomCount : 0f;
            itemQa.setDiscriminationIndex(itemDisc);

            itemQa.setIsTooEasy(rRate > 0.90f);
            itemQa.setIsLowDiscrimination(itemDisc < 0.20f);

            if (itemQa.getIsTooEasy() && itemQa.getIsLowDiscrimination()) {
                itemQa.setDiagnosisTag("题目过于简单且无区分度");
            } else if (itemQa.getIsLowDiscrimination()) {
                itemQa.setDiagnosisTag("区分度较差，建议修改选项");
            }

            qAnalyses.add(itemQa);
        }

        if (!qAnalyses.isEmpty()) {
            questionAnalysisService.saveBatch(qAnalyses);
        }

        // 10. Generate and Persist Improvement Suggestions (in Chinese)
        jdbcTemplate.update("DELETE FROM improvement_suggestion WHERE exam_id = ?", examId);
        List<QuestionAnalysisDTO> dtos = computeMapper.getQuestionAnalysisDetailsByExamId(examId);
        ObjectMapper mapper = new ObjectMapper();

        for (QuestionAnalysisDTO dto : dtos) {
            // Rule 1: Difficulty Check
            if (dto.getCorrectResponseRate() != null) {
                if (dto.getCorrectResponseRate() < 0.3f) {
                    saveSuggestion(examId, dto.getQuestionId(), "Difficulty_Adj", "题目难度过高，建议修改题干或干扰项。");

                    // Negative keyword check
                    if (dto.getStem() != null && (dto.getStem().toUpperCase().contains("NOT") ||
                            dto.getStem().toUpperCase().contains("EXCEPT") ||
                            dto.getStem().contains("不") ||
                            dto.getStem().contains("除了"))) {
                        saveSuggestion(examId, dto.getQuestionId(), "Question_Content",
                                "这是一道难度较高的否定形式题目。建议在题干中加粗或高亮否定词 (如 'NOT', 'EXCEPT', '不', '除了')，以提醒学生。");
                    }
                } else if (dto.getCorrectResponseRate() > 0.9f) {
                    saveSuggestion(examId, dto.getQuestionId(), "Difficulty_Adj", "题目过于简单，请检查正确答案是否过于明显。");
                }
            }

            // Rule 2: Discrimination Check
            if (dto.getDiscriminationIndex() != null && dto.getDiscriminationIndex() < 0.2f) {
                saveSuggestion(examId, dto.getQuestionId(), "Question_Content", "区分度较低。高分学生可能会被模棱两可的选项迷惑，建议优化选项。");
            }

            // Parse Options JSON for detailed checks
            Map<String, String> optionsMap = null;
            if (dto.getOptionsJson() != null && !dto.getOptionsJson().isEmpty() &&
                    ("SingleChoice".equals(dto.getQuestionType()) || "MultipleChoice".equals(dto.getQuestionType()))) {
                try {
                    optionsMap = mapper.readValue(dto.getOptionsJson(), new TypeReference<Map<String, String>>() {
                    });
                } catch (Exception e) {
                    // Ignore
                }
            }

            // Rule 3: Option Distribution
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
                    // Ignore error
                }
            }

            // Rule 4: Length Bias Check
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

        log.info("Finished indicator computation for examId: {}", examId);
    }

    private void saveSuggestion(Long examId, Long questionId, String type, String text) {
        jdbcTemplate.update(
                "INSERT INTO improvement_suggestion (exam_id, question_id, suggestion_type, suggestion_text, generated_rule, is_implemented) VALUES (?, ?, ?, ?, ?, ?)",
                examId, questionId, type, text, text, false);
    }

    private double calculateStdDev(List<BigDecimal> data, BigDecimal mean) {
        if (data.size() <= 1)
            return 0.0;
        double sumSquareDiffs = 0.0;
        for (BigDecimal val : data) {
            double diff = val.doubleValue() - mean.doubleValue();
            sumSquareDiffs += diff * diff;
        }
        return Math.sqrt(sumSquareDiffs / data.size()); // Population StdDev
    }

    private void deleteExistingPaperAnalysis(Long examId) {
        jdbcTemplate.update("DELETE FROM examination_paper_quality_analysis WHERE exam_id = ?", examId);
    }

    private void deleteExistingItemAnalysis(Long examId) {
        jdbcTemplate.update("DELETE FROM question_quality_analysis WHERE exam_id = ?", examId);
    }
}
