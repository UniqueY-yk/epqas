package com.epqas.academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.academic.entity.SchoolClass;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SchoolClassService extends IService<SchoolClass> {
    Page<SchoolClass> getClassesPage(Integer page, Integer size, String className);
}
