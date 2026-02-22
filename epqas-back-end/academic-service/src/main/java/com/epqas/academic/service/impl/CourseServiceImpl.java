package com.epqas.academic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.academic.entity.Course;
import com.epqas.academic.mapper.CourseMapper;
import com.epqas.academic.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Override
    public Page<Course> getCoursesPage(Integer page, Integer size, String courseName) {
        Page<Course> coursePage = new Page<>(page, size);
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        if (courseName != null && !courseName.isEmpty()) {
            queryWrapper.like("course_name", courseName);
        }
        return this.page(coursePage, queryWrapper);
    }
}
