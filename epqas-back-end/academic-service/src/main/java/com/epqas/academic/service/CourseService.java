package com.epqas.academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.academic.entity.Course;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface CourseService extends IService<Course> {
    /**
     * 获取课程分页数据
     * 
     * @param page       页码
     * @param size       每页数量
     * @param courseName 课程名称
     * @return 课程分页数据
     */
    Page<Course> getCoursesPage(Integer page, Integer size, String courseName);
}
