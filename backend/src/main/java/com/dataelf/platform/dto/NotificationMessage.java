package com.dataelf.platform.dto;

import com.dataelf.platform.entity.Notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Long relatedContentId;
}
