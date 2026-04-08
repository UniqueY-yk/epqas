package com.epqas.academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.academic.entity.Student;

import com.epqas.academic.dto.StudentDTO;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService extends IService<Student> {
    /**
     * 创建学生并关联用户
     * 
     * @param dto 学生数据传输对象
     */
    void createStudentWithUser(StudentDTO studentDTO);

    /**
     * 导入学生
     * 
     * @param file Excel文件
     */
    void importStudents(MultipartFile file);

    /**
     * 根据班级ID获取学生列表
     * 
     * @param classId 班级ID
     * @return 学生列表
     */
    java.util.List<Student> getStudentsByClassId(Integer classId);
}
