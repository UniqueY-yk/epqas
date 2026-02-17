package com.epqas.academic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "student")
@TableName("student")
public class Student {
    /**
     * Links to user.user_id
     */
    @Id
    @TableId
    private Long studentId;
    
    private Integer classId;
    
    private String studentNumber;
}
