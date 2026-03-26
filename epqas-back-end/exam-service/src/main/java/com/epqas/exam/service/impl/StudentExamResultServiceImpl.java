package com.epqas.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.mapper.StudentExamResultMapper;
import com.epqas.exam.service.StudentExamResultService;
import com.epqas.exam.service.StudentAnswerService;
import com.epqas.exam.entity.StudentAnswer;
import com.epqas.exam.dto.StudentExamResultBatchDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentExamResultServiceImpl extends ServiceImpl<StudentExamResultMapper, StudentExamResult> implements StudentExamResultService {

    @Autowired
    private StudentAnswerService studentAnswerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveResultAndAnswers(StudentExamResultBatchDTO dto) {
        StudentExamResult result = dto.getResult();
        
        // 1. Save or Update StudentExamResult
        if (result.getResultId() == null) {
            // Check if user already has a result for this exam
            StudentExamResult existing = this.getOne(new QueryWrapper<StudentExamResult>()
                    .eq("exam_id", result.getExamId())
                    .eq("student_id", result.getStudentId()));
            if (existing != null) {
                result.setResultId(existing.getResultId());
                this.updateById(result);
            } else {
                result.setSubmittedAt(LocalDateTime.now());
                this.save(result);
            }
        } else {
            this.updateById(result);
        }

        // 2. Save or Update StudentAnswers
        List<StudentAnswer> answers = dto.getAnswers();
        if (answers != null && !answers.isEmpty()) {
            for (StudentAnswer answer : answers) {
                answer.setResultId(result.getResultId());
                if (answer.getAnswerId() == null) {
                    StudentAnswer existingAns = studentAnswerService.getOne(new QueryWrapper<StudentAnswer>()
                            .eq("result_id", result.getResultId())
                            .eq("question_id", answer.getQuestionId()));
                    if (existingAns != null) {
                        answer.setAnswerId(existingAns.getAnswerId());
                        studentAnswerService.updateById(answer);
                    } else {
                        studentAnswerService.save(answer);
                    }
                } else {
                    studentAnswerService.updateById(answer);
                }
            }
        }
        return true;
    }
}
