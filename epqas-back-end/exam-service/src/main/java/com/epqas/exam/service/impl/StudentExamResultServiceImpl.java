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

    /**
     * 保存考试结果和答案
     * @param dto 考试结果和答案
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveResultAndAnswers(StudentExamResultBatchDTO dto) {
        StudentExamResult result = dto.getResult();
        
        // 1. 保存或更新考试结果
        if (result.getResultId() == null) {
            // 检查用户是否已经有该考试的结果
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

        // 2. 保存或更新学生答案
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
