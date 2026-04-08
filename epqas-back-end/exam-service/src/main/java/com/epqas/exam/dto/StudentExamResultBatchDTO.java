package com.epqas.exam.dto;

import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.entity.StudentAnswer;
import lombok.Data;

import java.util.List;

@Data
public class StudentExamResultBatchDTO {
    private StudentExamResult result; // 考试结果

    private List<StudentAnswer> answers; // 学生答案
}
