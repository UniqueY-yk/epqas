package com.epqas.academic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "school_class")
@TableName("school_class")
public class SchoolClass {
    @Id
    @TableId(type = IdType.AUTO)
    private Integer classId;
    private String className;
    private String department;
}
