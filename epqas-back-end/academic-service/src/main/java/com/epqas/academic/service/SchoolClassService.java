package com.epqas.academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.academic.entity.SchoolClass;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SchoolClassService extends IService<SchoolClass> {
    /**
     * 获取班级分页数据
     * 
     * @param page      页码
     * @param size      每页数量
     * @param className 班级名称
     * @return 班级分页数据
     */
    Page<SchoolClass> getClassesPage(Integer page, Integer size, String className);
}
