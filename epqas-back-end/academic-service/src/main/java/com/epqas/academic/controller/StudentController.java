package com.epqas.academic.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.academic.entity.Student;
import com.epqas.academic.service.StudentService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public Result<Page<Student>> listStudents(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(studentService.page(new Page<>(page, size)));
    }

    @PostMapping
    public Result<Boolean> createStudent(@RequestBody Student student) {
        // Note: Realistically should create User first or handle distributed transaction
        return Result.success(studentService.save(student));
    }

    @PutMapping
    public Result<Boolean> updateStudent(@RequestBody Student student) {
        return Result.success(studentService.updateById(student));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteStudent(@PathVariable Long id) {
        return Result.success(studentService.removeById(id));
    }
}
