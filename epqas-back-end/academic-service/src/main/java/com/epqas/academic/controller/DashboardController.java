package com.epqas.academic.controller;

import com.epqas.academic.service.CourseService;
import com.epqas.academic.service.SchoolClassService;
import com.epqas.academic.service.StudentService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final CourseService courseService;
    private final SchoolClassService classService;
    private final StudentService studentService;

    @Autowired
    public DashboardController(CourseService courseService, SchoolClassService classService, StudentService studentService) {
        this.courseService = courseService;
        this.classService = classService;
        this.studentService = studentService;
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("courseCount", courseService.count());
        stats.put("classCount", classService.count());
        stats.put("studentCount", studentService.count());
        return Result.success(stats);
    }
}
