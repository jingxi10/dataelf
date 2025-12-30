package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    
    private Long id;
    private Long userId;
    private Long contentId;
    private String commentText;
    private LocalDateTime createdAt;
    private Boolean isPinned;
    private Boolean isDeleted;
    
    // 可选：用户信息
    private String userEmail;
}
