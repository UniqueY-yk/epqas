package com.epqas.academic.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ExcelStudentDTO {

    @ExcelProperty("Student Number")
    private String studentNumber;

    @ExcelProperty("Real Name")
    private String realName;

    @ExcelProperty("Username")
    private String username;

    @ExcelProperty("Email")
    private String email;

    // In a real system you might map "Class Name" -> ID, but for simplicity:
    @ExcelProperty("Class ID")
    private Integer classId;

    @ExcelProperty("Password")
    private String password;
}
