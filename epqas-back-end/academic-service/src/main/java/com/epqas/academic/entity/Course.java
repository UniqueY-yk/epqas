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
@Table(name = "course")
@TableName("course")
public class Course {
    @Id
    @TableId(type = IdType.AUTO)
    private Integer courseId;
    
    private String courseName;
    
    private String courseCode;
}
