package com.epqas.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epqas.exam.dto.AbnormalDetectionDTO;
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

    @Override
    public List<AbnormalDetectionDTO> detectAbnormalAnswers(Long examId) {
        // 1. Get exam and its paper
        Examination exam = examinationMapper.selectById(examId);
        if (exam == null) return Collections.emptyList();

        // 2. Get questions on this paper
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<ExaminationPaperQuestion>().eq("paper_id", exam.getPaperId())
                        .orderByAsc("question_order")
        );
        if (paperQuestions.isEmpty()) return Collections.emptyList();

        List<Long> questionIds = paperQuestions.stream()
                .map(ExaminationPaperQuestion::getQuestionId).collect(Collectors.toList());

        // 3. Get all non-absent results for this exam
        List<StudentExamResult> results = resultMapper.selectList(
                new QueryWrapper<StudentExamResult>().eq("exam_id", examId).eq("is_absent", false)
        );
        if (results.size() < 2) return Collections.emptyList();

        Map<Long, Long> resultToStudent = new HashMap<>();
        List<Long> resultIds = new ArrayList<>();
        for (StudentExamResult r : results) {
            resultToStudent.put(r.getResultId(), r.getStudentId());
            resultIds.add(r.getResultId());
        }

        // 4. Get all answers for these results
        List<StudentAnswer> allAnswers = answerMapper.selectList(
                new QueryWrapper<StudentAnswer>().in("result_id", resultIds)
                        .in("question_id", questionIds)
        );

        // Build per-student answer map: studentId -> (questionId -> studentChoice)
        Map<Long, Map<Long, String>> studentAnswerMap = new HashMap<>();
        for (StudentAnswer a : allAnswers) {
            Long studentId = resultToStudent.get(a.getResultId());
            if (studentId == null) continue;
            studentAnswerMap
                    .computeIfAbsent(studentId, k -> new HashMap<>())
                    .put(a.getQuestionId(), a.getStudentChoice());
        }

        // 5. Fetch correct answers and question details
        String questionIdStr = questionIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Map<String, Object>> questionRows = jdbcTemplate.queryForList(
                "SELECT question_id, question_content, correct_answer FROM question WHERE question_id IN (" + questionIdStr + ")"
        );
        Map<Long, String> correctAnswers = new HashMap<>();
        Map<Long, String> questionContents = new HashMap<>();
        for (Map<String, Object> row : questionRows) {
            Long qId = ((Number) row.get("question_id")).longValue();
            correctAnswers.put(qId, (String) row.get("correct_answer"));
            questionContents.put(qId, (String) row.get("question_content"));
        }

        // Build questionOrder lookup
        Map<Long, Integer> questionOrderMap = new HashMap<>();
        for (ExaminationPaperQuestion pq : paperQuestions) {
            questionOrderMap.put(pq.getQuestionId(), pq.getQuestionOrder());
        }

        // 6. Resolve student names
        List<Long> studentIds = new ArrayList<>(studentAnswerMap.keySet());
        Map<Long, String> studentNames = resolveStudentNames(studentIds);

        // 7. Pairwise comparison
        int totalQuestions = questionIds.size();
        List<AbnormalDetectionDTO> detectionResults = new ArrayList<>();

        for (int i = 0; i < studentIds.size(); i++) {
            for (int j = i + 1; j < studentIds.size(); j++) {
                Long studentA = studentIds.get(i);
                Long studentB = studentIds.get(j);
                Map<Long, String> answersA = studentAnswerMap.getOrDefault(studentA, Collections.emptyMap());
                Map<Long, String> answersB = studentAnswerMap.getOrDefault(studentB, Collections.emptyMap());

                List<Map<String, Object>> identicalWrongDetails = new ArrayList<>();

                for (Long qId : questionIds) {
                    String choiceA = answersA.get(qId);
                    String choiceB = answersB.get(qId);
                    String correct = correctAnswers.get(qId);

                    if (choiceA == null || choiceB == null || correct == null) continue;
                    if (choiceA.isBlank() || choiceB.isBlank()) continue;

                    // Normalize choices for comparison
                    String normA = normalizeChoice(choiceA);
                    String normB = normalizeChoice(choiceB);
                    String normCorrect = normalizeChoice(correct);

                    // Both gave the same answer AND it's wrong
                    if (normA.equals(normB) && !normA.equals(normCorrect)) {
                        Map<String, Object> detail = new LinkedHashMap<>();
                        detail.put("questionOrder", questionOrderMap.getOrDefault(qId, 0));
                        detail.put("questionContent", questionContents.getOrDefault(qId, ""));
                        detail.put("studentChoice", choiceA);
                        detail.put("correctAnswer", correct);
                        identicalWrongDetails.add(detail);
                    }
                }

                int identicalWrongCount = identicalWrongDetails.size();
                double similarityRate = totalQuestions == 0 ? 0.0 : (double) identicalWrongCount / totalQuestions;

                // Only flag if meets threshold: >= 3 identical wrong answers OR >= 50% similarity
                if (identicalWrongCount >= 3 || similarityRate >= 0.5) {
                    AbnormalDetectionDTO dto = new AbnormalDetectionDTO();
                    dto.setStudentIdA(studentA);
                    dto.setStudentNameA(studentNames.getOrDefault(studentA, "学生" + studentA));
                    dto.setStudentIdB(studentB);
                    dto.setStudentNameB(studentNames.getOrDefault(studentB, "学生" + studentB));
                    dto.setTotalQuestions(totalQuestions);
                    dto.setIdenticalWrongCount(identicalWrongCount);
                    dto.setSimilarityRate(similarityRate);
                    dto.setIdenticalWrongDetails(identicalWrongDetails);
                    detectionResults.add(dto);
                }
            }
        }

        // Sort by similarity rate descending (most suspicious first)
        detectionResults.sort((a, b) -> Double.compare(b.getSimilarityRate(), a.getSimilarityRate()));

        return detectionResults;
    }

    private String normalizeChoice(String choice) {
        if (choice == null) return "";
        // Remove brackets, quotes, spaces, and sort for multi-choice comparison
        String clean = choice.replaceAll("[\\[\\]\" ]", "").trim();
        if (clean.contains(",")) {
            String[] parts = clean.split(",");
            Arrays.sort(parts);
            return String.join(",", parts);
        }
        return clean;
    }

    private Map<Long, String> resolveStudentNames(List<Long> studentIds) {
        Map<Long, String> names = new HashMap<>();
        if (studentIds.isEmpty()) return names;

        try {
            String idStr = studentIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            // student.student_id is the student table PK, student.user_id links to user.user_id
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT s.student_id, u.real_name FROM student s " +
                            "JOIN user u ON s.user_id = u.user_id " +
                            "WHERE s.student_id IN (" + idStr + ")"
            );
            for (Map<String, Object> row : rows) {
                Long sid = ((Number) row.get("student_id")).longValue();
                String name = (String) row.get("real_name");
                names.put(sid, name);
            }
        } catch (Exception e) {
            // Fallback: use student IDs if cross-DB query fails
            for (Long sid : studentIds) {
                names.put(sid, "学生" + sid);
            }
        }

        return names;
    }
}
