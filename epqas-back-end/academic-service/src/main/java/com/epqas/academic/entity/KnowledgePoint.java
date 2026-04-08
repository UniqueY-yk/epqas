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
    private Integer pointId; // 知识点ID

    private Integer courseId; // 课程ID

    private String pointName; // 知识点名称

    private String description; // 知识点描述
}
