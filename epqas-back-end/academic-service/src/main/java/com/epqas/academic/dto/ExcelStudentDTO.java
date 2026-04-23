package com.epqas.academic.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel学生数据传输对象
 */
@Data
public class ExcelStudentDTO {

    @ExcelProperty(value = "班级", index = 0)
    private Integer classId; // 班级ID

    @ExcelProperty(value = "学号", index = 1)
    private String username; // 学号

    @ExcelProperty(value = "姓名", index = 2)
    private String realName; // 姓名

    @ExcelProperty(value = "邮箱", index = 3)
    private String email; // 邮箱
}
