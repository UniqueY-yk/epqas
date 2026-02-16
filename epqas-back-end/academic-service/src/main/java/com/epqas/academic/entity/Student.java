package com.epqas.academic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "students")
@TableName("students")
public class Student {
    @Id
    @TableId
    private Long studentId; // Links to users.user_id

    private Integer classId;

    @Column(unique = true)
    private String studentNumber;
}
