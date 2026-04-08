package com.epqas.question.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.question.entity.ExaminationPaper;
import com.epqas.question.mapper.ExaminationPaperMapper;
import com.epqas.question.service.ExaminationPaperService;
import org.springframework.stereotype.Service;

@Service
public class ExaminationPaperServiceImpl extends ServiceImpl<ExaminationPaperMapper, ExaminationPaper>
        implements ExaminationPaperService {
}
