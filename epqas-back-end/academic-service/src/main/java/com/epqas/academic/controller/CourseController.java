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
    public Result<Page<Course>> listCourses(@RequestHeader("X-Role-Id") Integer roleId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "courseName", required = false) String courseName) {
        if (roleId == null)
            return Result.error(401, "Unauthorized");
        return Result.success(courseService.getCoursesPage(page, size, courseName));
    }

    @PostMapping
    public Result<Boolean> createCourse(@RequestHeader("X-Role-Id") Integer roleId, @RequestBody Course course) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(courseService.save(course));
    }

    @PutMapping
    public Result<Boolean> updateCourse(@RequestHeader("X-Role-Id") Integer roleId, @RequestBody Course course) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(courseService.updateById(course));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCourse(@RequestHeader("X-Role-Id") Integer roleId, @PathVariable("id") Integer id) {
        if (roleId == null || roleId != 1)
            return Result.error(403, "Access Denied");
        return Result.success(courseService.removeById(id));
    }
}
