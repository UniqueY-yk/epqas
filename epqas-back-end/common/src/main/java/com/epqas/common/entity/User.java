package com.epqas.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
@TableName("user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long userId; // 用户ID

    @Column(nullable = false, unique = true)
    private String username; // 用户名

    @Column(nullable = false)
    private String passwordHash; // 密码哈希

    private String realName; // 真实姓名

    private Integer roleId; // 角色ID

    private String email; // 邮箱

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
