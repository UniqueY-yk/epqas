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
@Table(name = "knowledge_point")
@TableName("knowledge_point")
public class KnowledgePoint {
    @Id
    @TableId(type = IdType.AUTO)
    private Integer pointId;
    
    private Integer courseId;
    
    private String pointName;
    
    private String description;
}
