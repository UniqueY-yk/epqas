package com.epqas.academic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.academic.entity.SchoolClass;
import com.epqas.academic.mapper.SchoolClassMapper;
import com.epqas.academic.service.SchoolClassService;
import org.springframework.stereotype.Service;

@Service
public class SchoolClassServiceImpl extends ServiceImpl<SchoolClassMapper, SchoolClass> implements SchoolClassService {

    /**
     * 获取班级分页数据
     * 
     * @param page      页码
     * @param size      每页数量
     * @param className 班级名称
     * @return 班级分页数据
     */
    @Override
    public Page<SchoolClass> getClassesPage(Integer page, Integer size, String className) {
        Page<SchoolClass> classPage = new Page<>(page, size);
        QueryWrapper<SchoolClass> queryWrapper = new QueryWrapper<>();
        if (className != null && !className.isEmpty()) {
            queryWrapper.like("class_name", className);
        }
        return this.page(classPage, queryWrapper);
    }
}
