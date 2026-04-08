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

    /**
     * 获取学生列表
     * 
     * @param page 页码
     * @param size 每页数量
     * @return 学生列表
     */
    @GetMapping
    public Result<Page<Student>> listStudents(@RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(studentService.page(new Page<>(page, size)));
    }

    /**
     * 创建学生
     * 
     * @param studentDTO 学生信息
     * @return 是否创建成功
     */
    @PostMapping
    public Result<Boolean> createStudent(@RequestBody StudentDTO studentDTO) {
        studentService.createStudentWithUser(studentDTO);
        return Result.success(true);
    }

    /**
     * 导入学生
     * 
     * @param file Excel文件
     * @return 是否导入成功
     */
    @PostMapping("/import")
    public Result<Boolean> importStudents(@RequestParam("file") MultipartFile file) {
        studentService.importStudents(file);
        return Result.success(true);
    }

    /**
     * 更新学生
     * 
     * @param student 学生信息
     * @return 是否更新成功
     */
    @PutMapping
    public Result<Boolean> updateStudent(@RequestBody Student student) {
        return Result.success(studentService.updateById(student));
    }

    /**
     * 删除学生
     * 
     * @param id 学生ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteStudent(@PathVariable("id") Long id) {
        return Result.success(studentService.removeById(id));
    }

    /**
     * 获取班级学生列表
     * 
     * @param classId 班级ID
     * @return 学生列表
     */
    @GetMapping("/class/{classId}")
    public Result<java.util.List<Student>> getStudentsByClassId(@PathVariable("classId") Integer classId) {
        return Result.success(studentService.getStudentsByClassId(classId));
    }
}
