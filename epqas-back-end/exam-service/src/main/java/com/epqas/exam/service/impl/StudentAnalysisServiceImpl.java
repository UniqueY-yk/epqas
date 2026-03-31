package com.epqas.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epqas.exam.dto.KnowledgeMasteryDTO;
import com.epqas.exam.entity.ExaminationPaperQuestion;
import com.epqas.exam.entity.StudentAnswer;
import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.mapper.ExaminationPaperQuestionMapper;
import com.epqas.exam.mapper.StudentAnswerMapper;
import com.epqas.exam.mapper.StudentExamResultMapper;
import com.epqas.exam.service.StudentAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentAnalysisServiceImpl implements StudentAnalysisService {

    @Autowired
    private StudentExamResultMapper resultMapper;

    @Autowired
    private StudentAnswerMapper answerMapper;

    @Autowired
    private ExaminationPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<KnowledgeMasteryDTO> getStudentKnowledgeMastery(Long studentId) {
        // 1. Fetch all completed exam results for this student
        List<StudentExamResult> results = resultMapper.selectList(
                new QueryWrapper<StudentExamResult>()
                        .eq("student_id", studentId)
                        .eq("is_absent", false)
        );
        if (results.isEmpty()) return Collections.emptyList();

        List<Long> resultIds = results.stream().map(StudentExamResult::getResultId).collect(Collectors.toList());

        // 2. Fetch all student answers belonging to those results
        List<StudentAnswer> allAnswers = answerMapper.selectList(
                new QueryWrapper<StudentAnswer>().in("result_id", resultIds)
        );
        if (allAnswers.isEmpty()) return Collections.emptyList();

        List<Long> questionIds = allAnswers.stream()
                .map(StudentAnswer::getQuestionId).distinct().collect(Collectors.toList());

        // Group answers by question ID for fast lookup
        Map<Long, List<StudentAnswer>> answersByQuestion = allAnswers.stream()
                .collect(Collectors.groupingBy(StudentAnswer::getQuestionId));

        // 3. Fetch paper question info (we need scoreValue for maxScore of each question)
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<ExaminationPaperQuestion>().in("question_id", questionIds)
        );
        Map<Long, BigDecimal> questionMaxScores = paperQuestions.stream()
                .collect(Collectors.toMap(
                    ExaminationPaperQuestion::getQuestionId, 
                    ExaminationPaperQuestion::getScoreValue,
                    (existing, replacement) -> existing
                ));

        // 4. Trace the knowledge points for these precise questions
        String questionIdStr = questionIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Map<String, Object>> kpRows = jdbcTemplate.queryForList(
                "SELECT qkp.question_id, kp.point_id, kp.point_name " +
                        "FROM question_knowledge_map qkp " +
                        "JOIN knowledge_point kp ON qkp.point_id = kp.point_id " +
                        "WHERE qkp.question_id IN (" + questionIdStr + ")"
        );

        // Map Point -> Set of Question IDs
        Map<Long, String> pointNames = new HashMap<>();
        Map<Long, Set<Long>> pointToQuestions = new HashMap<>();
        for (Map<String, Object> row : kpRows) {
            Long pointId = ((Number) row.get("point_id")).longValue();
            Long qId = ((Number) row.get("question_id")).longValue();
            String pName = (String) row.get("point_name");
            pointNames.put(pointId, pName);
            pointToQuestions.computeIfAbsent(pointId, k -> new HashSet<>()).add(qId);
        }

        // 5. Aggregate performance per Knowledge Point mathematically based on scores
        List<KnowledgeMasteryDTO> masteryList = new ArrayList<>();
        for (Map.Entry<Long, Set<Long>> entry : pointToQuestions.entrySet()) {
            Long pointId = entry.getKey();
            Set<Long> qIds = entry.getValue();

            int totalQuestionsEncountered = 0;
            BigDecimal totalMaxScore = BigDecimal.ZERO;
            BigDecimal totalObtainedScore = BigDecimal.ZERO;

            for (Long qId : qIds) {
                List<StudentAnswer> studentEfforts = answersByQuestion.getOrDefault(qId, Collections.emptyList());
                BigDecimal qMaxScore = questionMaxScores.getOrDefault(qId, BigDecimal.ZERO);
                
                for (StudentAnswer ans : studentEfforts) {
                    totalQuestionsEncountered++;
                    totalMaxScore = totalMaxScore.add(qMaxScore);
                    if (ans.getScoreObtained() != null) {
                        totalObtainedScore = totalObtainedScore.add(ans.getScoreObtained());
                    }
                }
            }

            KnowledgeMasteryDTO dto = new KnowledgeMasteryDTO();
            dto.setPointId(pointId);
            dto.setPointName(pointNames.get(pointId));
            dto.setTotalQuestions(totalQuestionsEncountered); // This is total *attempts*, effectively answers given
            dto.setTotalAnswers(totalQuestionsEncountered); 

            // Calculate true fractional mastery
            double masteryRate = 0.0;
            if (totalMaxScore.compareTo(BigDecimal.ZERO) > 0) {
                masteryRate = totalObtainedScore.divide(totalMaxScore, 4, java.math.RoundingMode.HALF_UP).doubleValue();
            }
            
            // For backward compatibility with the generic charts if they use purely correctCount
            // We can abstract correctCount = true completely accurate answers
            int perfectlyCorrect = 0;
            for (Long qId : qIds) {
                List<StudentAnswer> studentEfforts = answersByQuestion.getOrDefault(qId, Collections.emptyList());
                BigDecimal qMaxScore = questionMaxScores.getOrDefault(qId, BigDecimal.ZERO);
                for (StudentAnswer ans : studentEfforts) {
                    if (ans.getScoreObtained() != null && qMaxScore.compareTo(BigDecimal.ZERO) > 0 && ans.getScoreObtained().compareTo(qMaxScore) >= 0) {
                        perfectlyCorrect++;
                    }
                }
            }
            dto.setCorrectCount(perfectlyCorrect);
            dto.setMasteryRate(masteryRate);

            if (masteryRate >= 0.8) {
                dto.setLevel("excellent");
            } else if (masteryRate >= 0.6) {
                dto.setLevel("good");
            } else {
                dto.setLevel("weak");
            }

            masteryList.add(dto);
        }

        // Sort dynamically: weakest points physically surface at the top
        masteryList.sort(Comparator.comparingDouble(KnowledgeMasteryDTO::getMasteryRate));

        return masteryList;
    }
}
