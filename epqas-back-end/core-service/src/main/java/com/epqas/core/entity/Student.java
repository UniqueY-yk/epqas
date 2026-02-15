package com.epqas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "students")
@TableName("students")
public class Student {
    @Id
    @TableId(type = IdType.INPUT)
    private Long studentId;

    private Integer classId;

    @Column(unique = true)
    private String studentNumber;
}
