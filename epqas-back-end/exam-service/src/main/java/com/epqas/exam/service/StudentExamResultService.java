package com.epqas.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.dto.StudentExamResultBatchDTO;

public interface StudentExamResultService extends IService<StudentExamResult> {
    /**
     * 保存考试结果和答案
     * @param dto 考试结果和答案
     * @return 是否成功
     */
    Boolean saveResultAndAnswers(StudentExamResultBatchDTO dto);
}
