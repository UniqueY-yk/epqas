package com.epqas.academic.dto;

import lombok.Data;

/**
 * 学生数据传输对象
 */
@Data
public class StudentDTO {

    private Long userId; // 用户ID
    private String username; // 用户名
    private String password; // 密码
    private String realName; // 真实姓名
    private String email; // 邮箱

    private Integer classId; // 班级ID
    private String studentNumber; // 学号
}
