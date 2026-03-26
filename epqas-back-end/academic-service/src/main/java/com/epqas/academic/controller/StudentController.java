package com.epqas.academic.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.academic.entity.Student;
import com.epqas.academic.service.StudentService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.epqas.academic.dto.StudentDTO;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public Result<Page<Student>> listStudents(@RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(studentService.page(new Page<>(page, size)));
    }

    @PostMapping
    public Result<Boolean> createStudent(@RequestBody StudentDTO studentDTO) {
        studentService.createStudentWithUser(studentDTO);
        return Result.success(true);
    }

    @PostMapping("/import")
    public Result<Boolean> importStudents(@RequestParam("file") MultipartFile file) {
        studentService.importStudents(file);
        return Result.success(true);
    }

    @PutMapping
    public Result<Boolean> updateStudent(@RequestBody Student student) {
        return Result.success(studentService.updateById(student));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteStudent(@PathVariable("id") Long id) {
        return Result.success(studentService.removeById(id));
    }

    @GetMapping("/class/{classId}")
    public Result<java.util.List<Student>> getStudentsByClassId(@PathVariable("classId") Integer classId) {
        return Result.success(studentService.getStudentsByClassId(classId));
    }
}
