package com.epqas.academic.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.academic.entity.SchoolClass;
import com.epqas.academic.mapper.SchoolClassMapper;
import com.epqas.academic.service.SchoolClassService;
import org.springframework.stereotype.Service;

@Service
public class SchoolClassServiceImpl extends ServiceImpl<SchoolClassMapper, SchoolClass> implements SchoolClassService {
}
