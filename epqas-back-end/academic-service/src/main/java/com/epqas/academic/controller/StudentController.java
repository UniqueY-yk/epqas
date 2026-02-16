package com.epqas.academic.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.academic.entity.Student;
import com.epqas.academic.service.StudentService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping
    public Result<Boolean> createStudent(@RequestBody Student student) {
        return Result.success(studentService.save(student));
    }

    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable Long id) {
        return Result.success(studentService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateStudent(@RequestBody Student student) {
        return Result.success(studentService.updateById(student));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteStudent(@PathVariable Long id) {
        return Result.success(studentService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<Student>> getStudentPage(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(studentService.page(new Page<>(current, size)));
    }
    
    @GetMapping
    public Result<List<Student>> listStudents() {
        return Result.success(studentService.list());
    }
}
