package com.epqas.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epqas.exam.dto.KnowledgeMasteryDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.exam.dto.StudentErrorQuestionQuery;
import com.epqas.exam.entity.Examination;
import com.epqas.exam.entity.ExaminationPaper;
import com.epqas.exam.entity.ExaminationPaperQuestion;
import com.epqas.exam.entity.StudentAnswer;
import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.mapper.AnalysisDataMapper;
import com.epqas.exam.mapper.ExaminationMapper;
import com.epqas.exam.mapper.ExaminationPaperMapper;
import com.epqas.exam.mapper.ExaminationPaperQuestionMapper;
import com.epqas.exam.mapper.StudentAnswerMapper;
import com.epqas.exam.mapper.StudentExamResultMapper;
import com.epqas.exam.service.StudentAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ExaminationMapper examinationMapper;

    @Autowired
    private ExaminationPaperMapper examinationPaperMapper;

    @Autowired
    private AnalysisDataMapper analysisDataMapper;

    /**
     * 获取学生知识点掌握度
     * @param studentId 学生ID
     * @return 知识点掌握度列表
     */
    @Override
    public List<KnowledgeMasteryDTO> getStudentKnowledgeMastery(Long studentId) {
        // 1. 获取该学生所有完成的考试结果
        List<StudentExamResult> results = resultMapper.selectList(
                new QueryWrapper<StudentExamResult>()
                        .eq("student_id", studentId)
                        .eq("is_absent", false)
        );
        if (results.isEmpty()) return Collections.emptyList();

        List<Long> resultIds = results.stream().map(StudentExamResult::getResultId).collect(Collectors.toList());

        // 2. 获取属于这些结果的所有学生答案
        List<StudentAnswer> allAnswers = answerMapper.selectList(
                new QueryWrapper<StudentAnswer>().in("result_id", resultIds)
        );
        if (allAnswers.isEmpty()) return Collections.emptyList();

        List<Long> questionIds = allAnswers.stream()
                .map(StudentAnswer::getQuestionId).distinct().collect(Collectors.toList());

        // 按题目ID分组答案以便快速查找
        Map<Long, List<StudentAnswer>> answersByQuestion = allAnswers.stream()
                .collect(Collectors.groupingBy(StudentAnswer::getQuestionId));

        // 3. 获取试卷题目信息（我们需要 scoreValue 作为每道题的最大分数）
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<ExaminationPaperQuestion>().in("question_id", questionIds)
        );
        Map<Long, BigDecimal> questionMaxScores = paperQuestions.stream()
                .collect(Collectors.toMap(
                    ExaminationPaperQuestion::getQuestionId, 
                    ExaminationPaperQuestion::getScoreValue,
                    (existing, replacement) -> existing
                ));

        // 4. 追踪这些精确问题的知识点
        List<Map<String, Object>> kpRows = analysisDataMapper.selectKnowledgePointsByQuestionIds(questionIds);

        // 映射知识点 -> 题目ID集合
        Map<Long, String> pointNames = new HashMap<>();
        Map<Long, Set<Long>> pointToQuestions = new HashMap<>();
        for (Map<String, Object> row : kpRows) {
            Long pointId = ((Number) row.get("point_id")).longValue();
            Long qId = ((Number) row.get("question_id")).longValue();
            String pName = (String) row.get("point_name");
            pointNames.put(pointId, pName);
            pointToQuestions.computeIfAbsent(pointId, k -> new HashSet<>()).add(qId);
        }

        // 5. 基于分数按知识点汇总表现
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

            // 计算真实分数掌握率
            double masteryRate = 0.0;
            if (totalMaxScore.compareTo(BigDecimal.ZERO) > 0) {
                masteryRate = totalObtainedScore.divide(totalMaxScore, 4, java.math.RoundingMode.HALF_UP).doubleValue();
            }
            
            // 为了与使用纯粹 correctCount 的通用图表向后兼容
            // 我们可以将 correctCount 抽象为完全正确的答案
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

        // 动态排序：最薄弱的知识点物理上浮现到顶部
        masteryList.sort(Comparator.comparingDouble(KnowledgeMasteryDTO::getMasteryRate));

        return masteryList;
    }

    /**
     * 获取学生错题
     * @param query 错题查询条件
     * @return 错题列表
     */
    @Override
    public Page<com.epqas.exam.dto.StudentErrorQuestionDTO> getStudentErrorQuestions(StudentErrorQuestionQuery query) {
        Page<com.epqas.exam.dto.StudentErrorQuestionDTO> emptyPage = new Page<>(query.getCurrent(), query.getSize());
        
        // 1. 获取完成的考试结果
        QueryWrapper<StudentExamResult> rw = new QueryWrapper<StudentExamResult>()
                .eq("student_id", query.getStudentId())
                .eq("is_absent", false);
        if (query.getExamId() != null) {
            rw.eq("exam_id", query.getExamId());
        }
        
        List<StudentExamResult> results = resultMapper.selectList(rw);
        if (results.isEmpty()) return emptyPage;

        List<Long> resultIds = results.stream().map(StudentExamResult::getResultId).collect(Collectors.toList());
        List<Long> examIds = results.stream().map(StudentExamResult::getExamId).distinct().collect(Collectors.toList());

        // 2. 获取考试信息以供参考（名称、日期、试卷ID）
        List<Examination> exams = examinationMapper.selectList(new QueryWrapper<Examination>().in("exam_id", examIds));
        Map<Long, Examination> examMap = exams.stream().collect(Collectors.toMap(Examination::getExamId, e -> e));

        // 2.5 如果需要按课程过滤，则获取试卷
        if (query.getCourseId() != null) {
            List<Long> paperIds = exams.stream().map(Examination::getPaperId).distinct().collect(Collectors.toList());
            if (paperIds.isEmpty()) return emptyPage;
            
            List<ExaminationPaper> papers = examinationPaperMapper.selectList(
                new QueryWrapper<ExaminationPaper>().in("paper_id", paperIds).eq("course_id", query.getCourseId())
            );
            Set<Long> validPaperIds = papers.stream().map(ExaminationPaper::getPaperId).collect(Collectors.toSet());
            
            // 按课程有效试卷过滤考试和结果
            exams = exams.stream().filter(e -> validPaperIds.contains(e.getPaperId())).collect(Collectors.toList());
            if (exams.isEmpty()) return emptyPage;
            
            Set<Long> validExamIds = exams.stream().map(Examination::getExamId).collect(Collectors.toSet());
            results = results.stream().filter(r -> validExamIds.contains(r.getExamId())).collect(Collectors.toList());
            if (results.isEmpty()) return emptyPage;
            
            resultIds = results.stream().map(StudentExamResult::getResultId).collect(Collectors.toList());
        }

        Map<Long, Examination> resultToExamMap = new HashMap<>();
        for (StudentExamResult r : results) {
            resultToExamMap.put(r.getResultId(), examMap.get(r.getExamId()));
        }

        // 3. 获取所有有效结果的学生答案
        List<StudentAnswer> allAnswers = answerMapper.selectList(new QueryWrapper<StudentAnswer>().in("result_id", resultIds));
        if (allAnswers.isEmpty()) return emptyPage;

        List<Long> questionIds = allAnswers.stream().map(StudentAnswer::getQuestionId).distinct().collect(Collectors.toList());

        // 4. 加载完整的题目元数据（包括约束）
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<ExaminationPaperQuestion>().in("question_id", questionIds)
        );
        Map<Long, BigDecimal> maxScoreMap = paperQuestions.stream()
                .collect(Collectors.toMap(
                        ExaminationPaperQuestion::getQuestionId,
                        ExaminationPaperQuestion::getScoreValue,
                        (existing, replacement) -> existing
                ));

        List<Map<String, Object>> questionRows = analysisDataMapper.selectQuestionDetailsByIds(questionIds, query.getQuestionType());
        Map<Long, Map<String, Object>> questionMap = new HashMap<>();
        for (Map<String, Object> row : questionRows) {
            Long qId = ((Number) row.get("question_id")).longValue();
            questionMap.put(qId, row);
        }

        // 5. 检测并提取错误题目
        List<com.epqas.exam.dto.StudentErrorQuestionDTO> errorList = new ArrayList<>();
        List<Long> errorQuestionIds = new ArrayList<>(); 
        
        for (StudentAnswer ans : allAnswers) {
            Map<String, Object> q = questionMap.get(ans.getQuestionId());
            if (q == null) continue;

            BigDecimal maxScore = maxScoreMap.getOrDefault(ans.getQuestionId(), BigDecimal.ZERO);
            BigDecimal obtained = ans.getScoreObtained() != null ? ans.getScoreObtained() : BigDecimal.ZERO;

            if (maxScore.compareTo(BigDecimal.ZERO) > 0 && obtained.compareTo(maxScore) < 0) {
                com.epqas.exam.dto.StudentErrorQuestionDTO err = new com.epqas.exam.dto.StudentErrorQuestionDTO();
                err.setQuestionId(ans.getQuestionId());
                
                Examination ctx = resultToExamMap.get(ans.getResultId());
                if (ctx != null) {
                    err.setExamId(ctx.getExamId());
                    err.setExamDate(ctx.getExamDate());
                }
                
                err.setQuestionType((String) q.get("question_type"));
                err.setQuestionContent((String) q.get("question_content"));
                err.setOptionsJson((String) q.get("options_json"));
                err.setCorrectAnswer((String) q.get("correct_answer"));
                err.setStudentChoice(ans.getStudentChoice());
                err.setScoreObtained(obtained);
                err.setMaxScore(maxScore);
                
                errorList.add(err);
                errorQuestionIds.add(ans.getQuestionId());
            }
        }

        if (errorList.isEmpty()) return emptyPage;

        // 6. 按时间顺序排序（最近的错误按顺序显示）
        errorList.sort((a, b) -> {
            if (a.getExamDate() == null && b.getExamDate() == null) return 0;
            if (a.getExamDate() == null) return 1;
            if (b.getExamDate() == null) return -1;
            return b.getExamDate().compareTo(a.getExamDate());
        });

        // 7. 分页内存列表，然后再获取知识点
        int total = errorList.size();
        emptyPage.setTotal(total);
        
        int start = (query.getCurrent() - 1) * query.getSize();
        if (start >= total) {
            return emptyPage;
        }
        int end = Math.min(start + query.getSize(), total);
        List<com.epqas.exam.dto.StudentErrorQuestionDTO> paginatedList = errorList.subList(start, end);

        if (paginatedList.isEmpty()) return emptyPage;

        // 8. 将丰富的知识点映射到分页切片层，以节省大量的数据库连接开销！
        List<Long> errorIds = paginatedList.stream().map(com.epqas.exam.dto.StudentErrorQuestionDTO::getQuestionId).distinct().collect(Collectors.toList());
        List<Map<String, Object>> kpRows = analysisDataMapper.selectKnowledgePointsByQuestionIds(errorIds);
        Map<Long, List<String>> pointMap = new HashMap<>();
        for (Map<String, Object> row : kpRows) {
            Long qId = ((Number) row.get("question_id")).longValue();
            String pName = (String) row.get("point_name");
            pointMap.computeIfAbsent(qId, k -> new ArrayList<>()).add(pName);
        }

        for (com.epqas.exam.dto.StudentErrorQuestionDTO err : paginatedList) {
            err.setKnowledgePoints(pointMap.getOrDefault(err.getQuestionId(), new ArrayList<>()));
        }

        emptyPage.setRecords(paginatedList);
        return emptyPage;
    }
}
