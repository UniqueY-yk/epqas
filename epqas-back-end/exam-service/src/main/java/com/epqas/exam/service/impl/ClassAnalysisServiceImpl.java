package com.epqas.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epqas.exam.dto.AbnormalDetectionDTO;
import com.epqas.exam.dto.KnowledgeMasteryDTO;
import com.epqas.exam.dto.QuestionStatsDTO;
import com.epqas.exam.entity.Examination;
import com.epqas.exam.entity.ExaminationPaperQuestion;
import com.epqas.exam.entity.StudentAnswer;
import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.mapper.AnalysisDataMapper;
import com.epqas.exam.mapper.ExaminationMapper;
import com.epqas.exam.mapper.ExaminationPaperQuestionMapper;
import com.epqas.exam.mapper.StudentAnswerMapper;
import com.epqas.exam.mapper.StudentExamResultMapper;
import com.epqas.exam.service.ClassAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AnalysisDataMapper analysisDataMapper;

    /**
     * 获取题目统计数据
     * @param examId 考试ID
     * @return 题目统计数据
     */
    @Override
    public List<QuestionStatsDTO> getQuestionStats(Long examId) {
        // 1. 获取考试和试卷
        Examination exam = examinationMapper.selectById(examId);
        if (exam == null) return Collections.emptyList();

        // 2. 获取试卷上的题目
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<ExaminationPaperQuestion>().eq("paper_id", exam.getPaperId())
                        .orderByAsc("question_order")
        );
        if (paperQuestions.isEmpty()) return Collections.emptyList();

        // 3. 获取该考试的所有结果ID
        List<StudentExamResult> results = resultMapper.selectList(
                new QueryWrapper<StudentExamResult>().eq("exam_id", examId).eq("is_absent", false)
        );
        List<Long> resultIds = results.stream().map(StudentExamResult::getResultId).collect(Collectors.toList());
        if (resultIds.isEmpty()) return Collections.emptyList();

        // 4. 获取这些结果的所有答案
        List<StudentAnswer> allAnswers = answerMapper.selectList(
                new QueryWrapper<StudentAnswer>().in("result_id", resultIds)
        );

        // 按 questionId 分组答案
        Map<Long, List<StudentAnswer>> answersByQuestion = allAnswers.stream()
                .collect(Collectors.groupingBy(StudentAnswer::getQuestionId));

        // 5. 从 question 表获取问题详情
        List<Long> questionIds = paperQuestions.stream()
                .map(ExaminationPaperQuestion::getQuestionId).collect(Collectors.toList());
        List<Map<String, Object>> questionRows = analysisDataMapper.selectQuestionDetailsByIds(questionIds, null);
        Map<Long, Map<String, Object>> questionMap = new HashMap<>();
        for (Map<String, Object> row : questionRows) {
            Long qId = ((Number) row.get("question_id")).longValue();
            questionMap.put(qId, row);
        }

        // 6. 获取每一个问题的知识点
        List<Map<String, Object>> kpRows = analysisDataMapper.selectKnowledgePointsByQuestionIds(questionIds);
        Map<Long, List<String>> kpMap = new HashMap<>();
        for (Map<String, Object> row : kpRows) {
            Long qId = ((Number) row.get("question_id")).longValue();
            String pName = (String) row.get("point_name");
            kpMap.computeIfAbsent(qId, k -> new ArrayList<>()).add(pName);
        }

        // 7. 构建结果DTOs
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

            // 选项分布
            Map<String, Integer> distribution = new LinkedHashMap<>();
            for (StudentAnswer a : answers) {
                String choice = a.getStudentChoice();
                if (choice != null && !choice.isBlank()) {
                    // 对于多选题，学生可以选择多个选项，如 ["A","B"] 或 "A,B"
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

    /**
     * 获取知识点掌握度
     * @param examId 考试ID
     * @return 知识点掌握度
     */
    @Override
    public List<KnowledgeMasteryDTO> getKnowledgeMastery(Long examId) {
        // 1. 获取考试
        Examination exam = examinationMapper.selectById(examId);
        if (exam == null) return Collections.emptyList();

        // 2. 获取试卷上的题目
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<ExaminationPaperQuestion>().eq("paper_id", exam.getPaperId())
        );
        if (paperQuestions.isEmpty()) return Collections.emptyList();

        List<Long> questionIds = paperQuestions.stream()
                .map(ExaminationPaperQuestion::getQuestionId).collect(Collectors.toList());

        // 3. 获取结果ID
        List<StudentExamResult> results = resultMapper.selectList(
                new QueryWrapper<StudentExamResult>().eq("exam_id", examId).eq("is_absent", false)
        );
        List<Long> resultIds = results.stream().map(StudentExamResult::getResultId).collect(Collectors.toList());
        if (resultIds.isEmpty()) return Collections.emptyList();

        // 4. 获取所有答案
        List<StudentAnswer> allAnswers = answerMapper.selectList(
                new QueryWrapper<StudentAnswer>().in("result_id", resultIds)
                        .in("question_id", questionIds)
        );
        Map<Long, List<StudentAnswer>> answersByQuestion = allAnswers.stream()
                .collect(Collectors.groupingBy(StudentAnswer::getQuestionId));

        // 5. 获取对应知识点
        List<Map<String, Object>> kpRows = analysisDataMapper.selectKnowledgePointsByQuestionIds(questionIds);

        // 分组: pointId -> list of questionIds
        Map<Long, String> pointNames = new HashMap<>();
        Map<Long, Set<Long>> pointToQuestions = new HashMap<>();
        for (Map<String, Object> row : kpRows) {
            Long pointId = ((Number) row.get("point_id")).longValue();
            Long qId = ((Number) row.get("question_id")).longValue();
            String pName = (String) row.get("point_name");
            pointNames.put(pointId, pName);
            pointToQuestions.computeIfAbsent(pointId, k -> new HashSet<>()).add(qId);
        }

        // 6. 计算每个知识点的掌握程度
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

        // 按掌握程度升序排序（最弱的在前）
        masteryList.sort(Comparator.comparingDouble(KnowledgeMasteryDTO::getMasteryRate));

        return masteryList;
    }

    /**
     * 检测异常答案
     * @param examId 考试ID
     * @return 异常答案列表
     */
    @Override
    public List<AbnormalDetectionDTO> detectAbnormalAnswers(Long examId) {
        // 1. 获取考试和试卷
        Examination exam = examinationMapper.selectById(examId);
        if (exam == null) return Collections.emptyList();

        // 2. 获取试卷上的题目
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<ExaminationPaperQuestion>().eq("paper_id", exam.getPaperId())
                        .orderByAsc("question_order")
        );
        if (paperQuestions.isEmpty()) return Collections.emptyList();

        List<Long> questionIds = paperQuestions.stream()
                .map(ExaminationPaperQuestion::getQuestionId).collect(Collectors.toList());

        // 3. 获取该考试的所有非缺考结果
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

        // 4. 获取这些结果的所有答案
        List<StudentAnswer> allAnswers = answerMapper.selectList(
                new QueryWrapper<StudentAnswer>().in("result_id", resultIds)
                        .in("question_id", questionIds)
        );

        // 构建每个学生的答案映射：studentId -> (questionId -> studentChoice)
        Map<Long, Map<Long, String>> studentAnswerMap = new HashMap<>();
        for (StudentAnswer a : allAnswers) {
            Long studentId = resultToStudent.get(a.getResultId());
            if (studentId == null) continue;
            studentAnswerMap
                    .computeIfAbsent(studentId, k -> new HashMap<>())
                    .put(a.getQuestionId(), a.getStudentChoice());
        }

        // 5. 获取正确答案和问题详情
        List<Map<String, Object>> questionRows = analysisDataMapper.selectQuestionDetailsByIds(questionIds, null);
        Map<Long, String> correctAnswers = new HashMap<>();
        Map<Long, String> questionContents = new HashMap<>();
        for (Map<String, Object> row : questionRows) {
            Long qId = ((Number) row.get("question_id")).longValue();
            correctAnswers.put(qId, (String) row.get("correct_answer"));
            questionContents.put(qId, (String) row.get("question_content"));
        }

        // 构建 questionOrder 映射
        Map<Long, Integer> questionOrderMap = new HashMap<>();
        for (ExaminationPaperQuestion pq : paperQuestions) {
            questionOrderMap.put(pq.getQuestionId(), pq.getQuestionOrder());
        }

        // 6. 解析学生姓名
        List<Long> studentIds = new ArrayList<>(studentAnswerMap.keySet());
        Map<Long, String> studentNames = resolveStudentNames(studentIds);

        // 7. 成对比较
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

                    // 规范化选择以进行比较
                    String normA = normalizeChoice(choiceA);
                    String normB = normalizeChoice(choiceB);
                    String normCorrect = normalizeChoice(correct);

                    // 两人给出了相同的答案且答案是错误的
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

                // 仅当满足严格的阈值时标记（避免误报过多）：
                // 1. 相同的错误答案数量 >= 5 且 相同错误率 >= 30%
                // 2. 或者绝对相同错误答案数量 >= 10
                if ((identicalWrongCount >= 5 && similarityRate >= 0.45) || identicalWrongCount >= 10) {
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

        // 按相似度降序排序（最可疑的在前）
        detectionResults.sort((a, b) -> Double.compare(b.getSimilarityRate(), a.getSimilarityRate()));

        return detectionResults;
    }

    /**
     * 规范化答案
     * @param choice 答案
     * @return 规范化后的答案
     */
    private String normalizeChoice(String choice) {
        if (choice == null) return "";
        // 移除括号、引号、空格并排序以进行多选比较
        String clean = choice.replaceAll("[\\[\\]\" ]", "").trim();
        if (clean.contains(",")) {
            String[] parts = clean.split(",");
            Arrays.sort(parts);
            return String.join(",", parts);
        }
        return clean;
    }

    /**
     * 解析学生姓名
     * @param studentIds 学生ID列表
     * @return 学生姓名映射
     */
    private Map<Long, String> resolveStudentNames(List<Long> studentIds) {
        Map<Long, String> names = new HashMap<>();
        if (studentIds.isEmpty()) return names;

        try {
            // student.student_id 是学生表主键，student.user_id 链接到 user.user_id
            List<Map<String, Object>> rows = analysisDataMapper.selectStudentNamesByIds(studentIds);
            for (Map<String, Object> row : rows) {
                Long sid = ((Number) row.get("student_id")).longValue();
                String name = (String) row.get("real_name");
                names.put(sid, name);
            }
        } catch (Exception e) {
            // 降级：如果跨数据库查询失败，则使用学生ID
            for (Long sid : studentIds) {
                names.put(sid, "学生" + sid);
            }
        }

        return names;
    }
}
