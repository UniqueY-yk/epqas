package com.epqas.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epqas.exam.dto.KnowledgeMasteryDTO;
import com.epqas.exam.dto.QuestionStatsDTO;
import com.epqas.exam.entity.Examination;
import com.epqas.exam.entity.ExaminationPaperQuestion;
import com.epqas.exam.entity.StudentAnswer;
import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.mapper.ExaminationMapper;
import com.epqas.exam.mapper.ExaminationPaperQuestionMapper;
import com.epqas.exam.mapper.StudentAnswerMapper;
import com.epqas.exam.mapper.StudentExamResultMapper;
import com.epqas.exam.service.ClassAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClassAnalysisServiceImpl implements ClassAnalysisService {

    @Autowired
    private ExaminationMapper examinationMapper;

    @Autowired
    private ExaminationPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private StudentExamResultMapper resultMapper;

    @Autowired
    private StudentAnswerMapper answerMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<QuestionStatsDTO> getQuestionStats(Long examId) {
        // 1. Get exam and its paper
        Examination exam = examinationMapper.selectById(examId);
        if (exam == null) return Collections.emptyList();

        // 2. Get questions on this paper
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<ExaminationPaperQuestion>().eq("paper_id", exam.getPaperId())
                        .orderByAsc("question_order")
        );
        if (paperQuestions.isEmpty()) return Collections.emptyList();

        // 3. Get all result IDs for this exam
        List<StudentExamResult> results = resultMapper.selectList(
                new QueryWrapper<StudentExamResult>().eq("exam_id", examId).eq("is_absent", false)
        );
        List<Long> resultIds = results.stream().map(StudentExamResult::getResultId).collect(Collectors.toList());
        if (resultIds.isEmpty()) return Collections.emptyList();

        // 4. Get all answers for these results
        List<StudentAnswer> allAnswers = answerMapper.selectList(
                new QueryWrapper<StudentAnswer>().in("result_id", resultIds)
        );

        // Group answers by questionId
        Map<Long, List<StudentAnswer>> answersByQuestion = allAnswers.stream()
                .collect(Collectors.groupingBy(StudentAnswer::getQuestionId));

        // 5. Fetch question details from question table
        List<Long> questionIds = paperQuestions.stream()
                .map(ExaminationPaperQuestion::getQuestionId).collect(Collectors.toList());

        String questionIdStr = questionIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Map<String, Object>> questionRows = jdbcTemplate.queryForList(
                "SELECT question_id, question_content, question_type, correct_answer, options_json " +
                        "FROM question WHERE question_id IN (" + questionIdStr + ")"
        );
        Map<Long, Map<String, Object>> questionMap = new HashMap<>();
        for (Map<String, Object> row : questionRows) {
            Long qId = ((Number) row.get("question_id")).longValue();
            questionMap.put(qId, row);
        }

        // 6. Fetch knowledge points for questions
        List<Map<String, Object>> kpRows = jdbcTemplate.queryForList(
                "SELECT qkp.question_id, kp.point_name FROM question_knowledge_map qkp " +
                        "JOIN knowledge_point kp ON qkp.point_id = kp.point_id " +
                        "WHERE qkp.question_id IN (" + questionIdStr + ")"
        );
        Map<Long, List<String>> kpMap = new HashMap<>();
        for (Map<String, Object> row : kpRows) {
            Long qId = ((Number) row.get("question_id")).longValue();
            String pName = (String) row.get("point_name");
            kpMap.computeIfAbsent(qId, k -> new ArrayList<>()).add(pName);
        }

        // 7. Build result DTOs
        List<QuestionStatsDTO> statsList = new ArrayList<>();
        for (ExaminationPaperQuestion pq : paperQuestions) {
            Long qId = pq.getQuestionId();
            QuestionStatsDTO dto = new QuestionStatsDTO();
            dto.setQuestionId(qId);
            dto.setQuestionOrder(pq.getQuestionOrder());

            Map<String, Object> qInfo = questionMap.get(qId);
            if (qInfo != null) {
                dto.setQuestionContent((String) qInfo.get("question_content"));
                dto.setQuestionType((String) qInfo.get("question_type"));
                dto.setCorrectAnswer((String) qInfo.get("correct_answer"));
            }

            List<StudentAnswer> answers = answersByQuestion.getOrDefault(qId, Collections.emptyList());
            dto.setTotalAnswers(answers.size());

            int correct = (int) answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
            dto.setCorrectCount(correct);
            dto.setCorrectRate(answers.isEmpty() ? 0.0 : (double) correct / answers.size());

            // Option distribution
            Map<String, Integer> distribution = new LinkedHashMap<>();
            for (StudentAnswer a : answers) {
                String choice = a.getStudentChoice();
                if (choice != null && !choice.isBlank()) {
                    // For multiple choice, a student may choose multiple options like ["A","B"] or "A,B"
                    String cleanChoice = choice.replaceAll("[\\[\\]\" ]", ""); 
                    if (cleanChoice.contains(",")) {
                        String[] opts = cleanChoice.split(",");
                        for (String opt : opts) {
                            if (!opt.isEmpty()) {
                                 distribution.merge(opt.trim(), 1, (countA, countB) -> countA + countB);
                            }
                        }
                    } else if (!cleanChoice.isEmpty()) {
                        distribution.merge(cleanChoice.trim(), 1, (countA, countB) -> countA + countB);
                    }
                }
            }
            dto.setOptionDistribution(distribution);
            dto.setKnowledgePoints(kpMap.getOrDefault(qId, Collections.emptyList()));

            statsList.add(dto);
        }

        return statsList;
    }

    @Override
    public List<KnowledgeMasteryDTO> getKnowledgeMastery(Long examId) {
        // 1. Get exam
        Examination exam = examinationMapper.selectById(examId);
        if (exam == null) return Collections.emptyList();

        // 2. Get questions on this paper
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<ExaminationPaperQuestion>().eq("paper_id", exam.getPaperId())
        );
        if (paperQuestions.isEmpty()) return Collections.emptyList();

        List<Long> questionIds = paperQuestions.stream()
                .map(ExaminationPaperQuestion::getQuestionId).collect(Collectors.toList());
        String questionIdStr = questionIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        // 3. Get result IDs
        List<StudentExamResult> results = resultMapper.selectList(
                new QueryWrapper<StudentExamResult>().eq("exam_id", examId).eq("is_absent", false)
        );
        List<Long> resultIds = results.stream().map(StudentExamResult::getResultId).collect(Collectors.toList());
        if (resultIds.isEmpty()) return Collections.emptyList();

        // 4. Get all answers
        List<StudentAnswer> allAnswers = answerMapper.selectList(
                new QueryWrapper<StudentAnswer>().in("result_id", resultIds)
                        .in("question_id", questionIds)
        );
        Map<Long, List<StudentAnswer>> answersByQuestion = allAnswers.stream()
                .collect(Collectors.groupingBy(StudentAnswer::getQuestionId));

        // 5. Get knowledge point mappings
        List<Map<String, Object>> kpRows = jdbcTemplate.queryForList(
                "SELECT qkp.question_id, kp.point_id, kp.point_name " +
                        "FROM question_knowledge_map qkp " +
                        "JOIN knowledge_point kp ON qkp.point_id = kp.point_id " +
                        "WHERE qkp.question_id IN (" + questionIdStr + ")"
        );

        // Group: pointId -> list of questionIds
        Map<Long, String> pointNames = new HashMap<>();
        Map<Long, Set<Long>> pointToQuestions = new HashMap<>();
        for (Map<String, Object> row : kpRows) {
            Long pointId = ((Number) row.get("point_id")).longValue();
            Long qId = ((Number) row.get("question_id")).longValue();
            String pName = (String) row.get("point_name");
            pointNames.put(pointId, pName);
            pointToQuestions.computeIfAbsent(pointId, k -> new HashSet<>()).add(qId);
        }

        // 6. Calculate mastery per knowledge point
        List<KnowledgeMasteryDTO> masteryList = new ArrayList<>();
        for (Map.Entry<Long, Set<Long>> entry : pointToQuestions.entrySet()) {
            Long pointId = entry.getKey();
            Set<Long> qIds = entry.getValue();

            int total = 0;
            int correct = 0;
            for (Long qId : qIds) {
                List<StudentAnswer> answers = answersByQuestion.getOrDefault(qId, Collections.emptyList());
                total += answers.size();
                correct += (int) answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
            }

            KnowledgeMasteryDTO dto = new KnowledgeMasteryDTO();
            dto.setPointId(pointId);
            dto.setPointName(pointNames.get(pointId));
            dto.setTotalQuestions(qIds.size());
            dto.setTotalAnswers(total);
            dto.setCorrectCount(correct);
            dto.setMasteryRate(total == 0 ? 0.0 : (double) correct / total);

            if (dto.getMasteryRate() >= 0.8) {
                dto.setLevel("excellent");
            } else if (dto.getMasteryRate() >= 0.6) {
                dto.setLevel("good");
            } else {
                dto.setLevel("weak");
            }

            masteryList.add(dto);
        }

        // Sort by mastery rate ascending (weakest first)
        masteryList.sort(Comparator.comparingDouble(KnowledgeMasteryDTO::getMasteryRate));

        return masteryList;
    }
}
