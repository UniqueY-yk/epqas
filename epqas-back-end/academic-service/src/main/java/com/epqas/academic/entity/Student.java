package com.epqas.academic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student")
public class Student {
    @TableId
    private Long studentId; // 学生ID

    private Integer classId; // 班级ID

    private String studentNumber; // 学号
}
