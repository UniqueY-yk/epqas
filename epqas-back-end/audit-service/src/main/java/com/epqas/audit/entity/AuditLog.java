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

    private String actionType; // 操作类型

    private String targetTable; // 目标表

    private Long targetId; // 目标ID

    private String ipAddress; // IP地址

    @Column(updatable = false)
    private LocalDateTime actionTime; // 操作时间

    @PrePersist
    protected void onCreate() {
        actionTime = LocalDateTime.now();
    }
}
