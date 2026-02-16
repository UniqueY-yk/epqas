package com.epqas.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.exam.entity.StudentAnswer;
import com.epqas.exam.mapper.StudentAnswerMapper;
import com.epqas.exam.service.StudentAnswerService;
import org.springframework.stereotype.Service;

@Service
public class StudentAnswerServiceImpl extends ServiceImpl<StudentAnswerMapper, StudentAnswer> implements StudentAnswerService {
}
