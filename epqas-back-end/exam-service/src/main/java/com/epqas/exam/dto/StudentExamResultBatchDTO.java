package com.epqas.exam.dto;

import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.entity.StudentAnswer;
import lombok.Data;

import java.util.List;

@Data
public class StudentExamResultBatchDTO {
    private StudentExamResult result;
    private List<StudentAnswer> answers;
}
