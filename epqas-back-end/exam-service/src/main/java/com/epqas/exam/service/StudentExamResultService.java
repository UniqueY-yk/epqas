package com.epqas.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.dto.StudentExamResultBatchDTO;

public interface StudentExamResultService extends IService<StudentExamResult> {
    Boolean saveResultAndAnswers(StudentExamResultBatchDTO dto);
}
