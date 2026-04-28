package com.epqas.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_log")
@TableName("audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long logId; // 审计日志ID

    private Long userId; // 用户ID

    private String actionType; // 操作类型（如 CREATE / UPDATE / DELETE / QUERY）

    private String targetTable; // 操作模块描述（来自 @SystemLog 注解）

    private Long targetId; // 目标ID（保留字段，可选）

    private String ipAddress; // IP地址

    private String requestMethod; // 请求方法（GET / POST / PUT / DELETE）

    private String requestUrl; // 请求URL

    @Column(columnDefinition = "TEXT")
    private String requestParams; // 请求参数（JSON格式）

    private Long duration; // 执行耗时（毫秒）

    private Integer status; // 操作状态（0=成功，1=失败）

    private String errorMsg; // 错误信息（失败时记录）

    @Column(updatable = false)
    private LocalDateTime actionTime; // 操作时间

    @PrePersist
    protected void onCreate() {
        actionTime = LocalDateTime.now();
    }
}
