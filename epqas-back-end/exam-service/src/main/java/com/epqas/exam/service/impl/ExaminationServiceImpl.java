package com.epqas.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.exam.entity.Examination;
import com.epqas.exam.mapper.ExaminationMapper;
import com.epqas.exam.service.ExaminationService;
import org.springframework.stereotype.Service;

@Service
public class ExaminationServiceImpl extends ServiceImpl<ExaminationMapper, Examination> implements ExaminationService {
}
