package com.epqas.academic.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel学生数据传输对象
 */
@Data
public class ExcelStudentDTO {

    @ExcelProperty("Student Number")
    private String studentNumber; // 学号

    @ExcelProperty("Real Name")
    private String realName; // 真实姓名

    @ExcelProperty("Username")
    private String username; // 用户名

    @ExcelProperty("Email")
    private String email; // 邮箱

    @ExcelProperty("Class ID")
    private Integer classId; // 班级ID

    @ExcelProperty("Password")
    private String password; // 密码
}
