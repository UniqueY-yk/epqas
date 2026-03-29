package com.epqas.academic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student")
public class Student {
    /**
     * Links to user.user_id
     */
    @TableId
    private Long studentId;
    
    private Integer classId;
    
    private String studentNumber;
}
