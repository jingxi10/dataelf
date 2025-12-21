package com.dataelf.platform.dto;

import com.dataelf.platform.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    
    private Long id;
    private Long userId;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private Long relatedContentId;
    private LocalDateTime createdAt;
    
    public static NotificationDTO fromEntity(Notification notification) {
        return new NotificationDTO(
            notification.getId(),
            notification.getUserId(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getIsRead(),
            notification.getRelatedContentId(),
            notification.getCreatedAt()
        );
    }
}
