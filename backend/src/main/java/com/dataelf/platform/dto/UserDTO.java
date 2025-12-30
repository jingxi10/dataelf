package com.dataelf.platform.dto;

import com.dataelf.platform.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    private String email;
    private String phone;
    private String nickname;
    private String role;
    private User.AdminType adminType;
    private String adminPermissions; // JSON格式的权限配置
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime expiresAt;
    private Long approvedBy; // 批准人ID
    private String approvedByName; // 批准人邮箱（用于显示）
    
    public static UserDTO fromEntity(User user) {
        return fromEntity(user, null);
    }
    
    public static UserDTO fromEntity(User user, User approver) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setNickname(user.getNickname());
        dto.setRole(user.getRole() != null ? user.getRole().name() : "USER");
        dto.setAdminType(user.getAdminType());
        dto.setAdminPermissions(user.getAdminPermissions());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setApprovedAt(user.getApprovedAt());
        dto.setExpiresAt(user.getExpiresAt());
        dto.setApprovedBy(user.getApprovedBy());
        if (approver != null) {
            dto.setApprovedByName(approver.getEmail());
        } else if (user.getApprovedBy() != null) {
            // 如果提供了批准人，使用批准人的邮箱
            dto.setApprovedByName(null); // 将在controller中查询填充
        }
        return dto;
    }
}
