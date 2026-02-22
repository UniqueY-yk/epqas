package com.epqas.academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.academic.entity.Course;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface CourseService extends IService<Course> {
    Page<Course> getCoursesPage(Integer page, Integer size, String courseName);
}
