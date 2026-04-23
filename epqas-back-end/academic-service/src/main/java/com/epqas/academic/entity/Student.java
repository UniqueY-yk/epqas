package com.epqas.academic.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.epqas.common.handler.EncryptTypeHandler;
import lombok.Data;

@Data
@TableName(value = "student", autoResultMap = true)
public class Student {
    @TableId
    private Long studentId; // 学生ID

    private Integer classId; // 班级ID
}
