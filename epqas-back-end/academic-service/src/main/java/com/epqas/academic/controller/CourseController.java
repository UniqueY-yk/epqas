package com.epqas.academic.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.academic.entity.Course;
import com.epqas.academic.service.CourseService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public Result<Page<Course>> listCourses(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(courseService.page(new Page<>(page, size)));
    }

    @PostMapping
    public Result<Boolean> createCourse(@RequestBody Course course) {
        return Result.success(courseService.save(course));
    }

    @PutMapping
    public Result<Boolean> updateCourse(@RequestBody Course course) {
        return Result.success(courseService.updateById(course));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCourse(@PathVariable Integer id) {
        return Result.success(courseService.removeById(id));
    }
}
