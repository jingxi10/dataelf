package com.dataelf.platform.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_type", length = 20)
    private AdminType adminType;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20,
            // 关键修改：显式指定数据库列类型为 enum，匹配数据库定义
            columnDefinition = "enum('PENDING','APPROVED','REJECTED','EXPIRED')"
    )
    private UserStatus status = UserStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "admin_permissions", columnDefinition = "JSON")
    private String adminPermissions; // JSON格式存储权限菜单配置，如：["user_approve", "user_delete", "content_review", "content_delete"]

    public enum UserStatus {
        PENDING,
        APPROVED,
        REJECTED,
        EXPIRED
    }
    
    public enum UserRole {
        USER,
        ADMIN
    }
    
    public enum AdminType {
        MAIN_ADMIN,    // 主管理员
        NORMAL_ADMIN   // 普通管理员
    }
}