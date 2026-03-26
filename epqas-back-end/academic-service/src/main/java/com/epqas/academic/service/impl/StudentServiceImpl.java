package com.epqas.academic.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.academic.entity.Student;
import com.epqas.academic.mapper.StudentMapper;
import com.epqas.academic.service.StudentService;
import com.alibaba.excel.EasyExcel;
import com.epqas.academic.dto.ExcelStudentDTO;
import com.epqas.academic.dto.StudentDTO;
import com.epqas.academic.listener.StudentExcelListener;
import com.epqas.common.entity.User;
import com.epqas.common.feign.UserFeignClient;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStudentWithUser(StudentDTO dto) {
        // 1. Create User via Auth Service Feign Client
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(dto.getPassword() != null ? dto.getPassword() : "123456"); // Default temp password
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setRoleId(4); // 4 = Student in our SQL init script

        // Call Auth Service with our simulated admin header (RoleId = 1)
        Result<Long> authResult = userFeignClient.createUser(1, user);

        if (authResult.getCode() != 200 || authResult.getData() == null) {
            throw new RuntimeException("Failed to create user account: " + authResult.getMessage());
        }

        Long userId = authResult.getData();

        // 2. Create Student record
        Student student = new Student();
        student.setStudentId(userId); // PK links to user.user_id
        student.setStudentNumber(dto.getStudentNumber());
        student.setClassId(dto.getClassId());

        this.save(student);
    }

    @Override
    public void importStudents(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), ExcelStudentDTO.class, new StudentExcelListener(this)).sheet()
                    .doRead();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }
    }

    @Override
    public java.util.List<Student> getStudentsByClassId(Integer classId) {
        return this.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Student>().eq("class_id", classId));
    }
}
