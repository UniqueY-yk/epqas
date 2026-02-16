package com.epqas.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.exam.entity.StudentExamResult;
import com.epqas.exam.mapper.StudentExamResultMapper;
import com.epqas.exam.service.StudentExamResultService;
import org.springframework.stereotype.Service;

@Service
public class StudentExamResultServiceImpl extends ServiceImpl<StudentExamResultMapper, StudentExamResult> implements StudentExamResultService {
}
