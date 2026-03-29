package com.epqas.academic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("school_class")
public class SchoolClass {
    @TableId(type = IdType.AUTO)
    private Integer classId;
    private String className;
    private String department;
}
