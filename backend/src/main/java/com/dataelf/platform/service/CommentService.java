package com.dataelf.platform.service;

import com.dataelf.platform.dto.CommentCreateRequest;
import com.dataelf.platform.dto.CommentDTO;
import com.dataelf.platform.entity.Comment;
import com.dataelf.platform.entity.Content;
import com.dataelf.platform.entity.User;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.CommentRepository;
import com.dataelf.platform.repository.ContentRepository;
import com.dataelf.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public CommentDTO createComment(Long userId, CommentCreateRequest request) {
        // 验证内容是否存在且已发布
        Content content = contentRepository.findById(request.getContentId())
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        if (content.getStatus() != Content.ContentStatus.PUBLISHED) {
            throw new ValidationException("只能评论已发布的内容");
        }
        
        // 创建评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setContentId(request.getContentId());
        comment.setCommentText(request.getCommentText());
        
        Comment saved = commentRepository.save(comment);
        log.info("User {} created comment on content {}", userId, request.getContentId());
        
        return convertToDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentsByContentId(Long contentId, Pageable pageable) {
        // 验证内容是否存在
        if (!contentRepository.existsById(contentId)) {
            throw new ValidationException("内容不存在");
        }
        
        // 只返回未删除的评论，置顶的在前
        Page<Comment> comments = commentRepository.findByContentIdAndIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(contentId, pageable);
        
        return comments.map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Long getCommentCount(Long contentId) {
        // 只统计未删除的评论
        return commentRepository.countByContentIdAndIsDeletedFalse(contentId);
    }
    
    /**
     * 删除评论
     * 权限规则：
     * 1. 评论作者可以删除自己的评论
     * 2. 文章作者可以删除自己文章下的所有评论
     * 3. 主管理员可以删除所有评论
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ValidationException("评论不存在"));
        
        // 检查是否是评论作者
        boolean isCommentAuthor = comment.getUserId().equals(userId);
        
        // 检查是否是文章作者
        Content content = contentRepository.findById(comment.getContentId())
            .orElseThrow(() -> new ValidationException("内容不存在"));
        boolean isContentAuthor = content.getUserId().equals(userId);
        
        // 检查是否是主管理员
        boolean isMainAdmin = false;
        if (isAdmin) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("用户不存在"));
            isMainAdmin = user.getRole() == User.UserRole.ADMIN && 
                         user.getAdminType() == User.AdminType.MAIN_ADMIN;
        }
        
        // 权限检查：评论作者、文章作者或主管理员可以删除
        if (!isCommentAuthor && !isContentAuthor && !isMainAdmin) {
            throw new ValidationException("无权限删除此评论");
        }
        
        comment.setIsDeleted(true);
        commentRepository.save(comment);
        log.info("Comment {} deleted by user {} (isCommentAuthor: {}, isContentAuthor: {}, isMainAdmin: {})", 
            commentId, userId, isCommentAuthor, isContentAuthor, isMainAdmin);
    }
    
    /**
     * 置顶/取消置顶评论（管理员权限）
     */
    @Transactional
    public void togglePinComment(Long commentId, boolean pin) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ValidationException("评论不存在"));
        
        comment.setIsPinned(pin);
        commentRepository.save(comment);
        log.info("Comment {} pin status set to {}", commentId, pin);
    }
    
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setUserId(comment.getUserId());
        dto.setContentId(comment.getContentId());
        dto.setCommentText(comment.getCommentText());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setIsPinned(comment.getIsPinned());
        dto.setIsDeleted(comment.getIsDeleted());
        
        // 可选：添加用户邮箱信息
        userRepository.findById(comment.getUserId()).ifPresent(user -> {
            dto.setUserEmail(maskEmail(user.getEmail()));
        });
        
        return dto;
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + parts[1];
        }
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + parts[1];
    }
}
