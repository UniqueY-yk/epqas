package com.epqas.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.epqas.common.handler.EncryptConverter;
import com.epqas.common.handler.EncryptTypeHandler;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
@TableName(value = "user", autoResultMap = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long userId; // 用户ID

    @Convert(converter = EncryptConverter.class)
    @TableField(typeHandler = EncryptTypeHandler.class)
    @Column(nullable = false, unique = true)
    private String username; // 用户名

    @Column(nullable = false)
    private String passwordHash; // 密码哈希

    @Convert(converter = EncryptConverter.class)
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String realName; // 姓名（AES加密存储）

    private Integer roleId; // 角色ID

    @Convert(converter = EncryptConverter.class)
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String email; // 邮箱（AES加密存储）

    @Column(updatable = false)
    private LocalDateTime createdAt; // 创建时间

    private Boolean isActive; // 是否激活

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }
}
