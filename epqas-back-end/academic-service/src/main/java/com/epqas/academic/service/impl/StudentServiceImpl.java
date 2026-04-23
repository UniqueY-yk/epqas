package com.epqas.academic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    private final UserFeignClient userFeignClient;

    /**
     * 构造函数
     * 
     * @param userFeignClient 用户服务客户端
     */
    @Autowired
    public StudentServiceImpl(UserFeignClient userFeignClient) {
        this.userFeignClient = userFeignClient;
    }

    /**
     * 创建学生并关联用户
     * 
     * @param dto 学生数据传输对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStudentWithUser(StudentDTO dto) {
        // 1. 调用认证服务创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(dto.getPassword() != null && !dto.getPassword().trim().isEmpty() ? dto.getPassword() : "123456"); // 默认临时密码
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setRoleId(4); // 4 = Student in our SQL init script

        // 调用认证服务
        Result<Long> authResult = userFeignClient.createUser(1, user);

        if (authResult.getCode() != 200 || authResult.getData() == null) {
            throw new RuntimeException("创建用户失败: " + authResult.getMessage());
        }

        Long userId = authResult.getData();

        // 2. 创建学生记录
        Student student = new Student();
        student.setStudentId(userId); // 主键链接到user.user_id
        student.setClassId(dto.getClassId());

        this.save(student);
    }

    /**
     * 导入学生
     * 
     * @param file Excel文件
     */
    @Override
    public void importStudents(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), ExcelStudentDTO.class, new StudentExcelListener(this)).sheet()
                    .doRead();
        } catch (IOException e) {
            throw new RuntimeException("解析Excel文件失败", e);
        }
    }

    /**
     * 根据班级ID获取学生列表
     * 
     * @param classId 班级ID
     * @return 学生列表
     */
    @Override
    public java.util.List<Student> getStudentsByClassId(Integer classId) {
        return this.list(new QueryWrapper<Student>().eq("class_id", classId));
    }
}
