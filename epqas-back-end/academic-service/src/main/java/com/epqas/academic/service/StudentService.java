package com.epqas.academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.academic.entity.Student;

import com.epqas.academic.dto.StudentDTO;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService extends IService<Student> {

    void createStudentWithUser(StudentDTO studentDTO);
    void importStudents(MultipartFile file);
    java.util.List<Student> getStudentsByClassId(Integer classId);
}
