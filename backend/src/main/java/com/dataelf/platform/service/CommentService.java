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
        
        Page<Comment> comments = commentRepository.findByContentIdOrderByCreatedAtDesc(contentId, pageable);
        
        return comments.map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Long getCommentCount(Long contentId) {
        return commentRepository.countByContentId(contentId);
    }
    
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setUserId(comment.getUserId());
        dto.setContentId(comment.getContentId());
        dto.setCommentText(comment.getCommentText());
        dto.setCreatedAt(comment.getCreatedAt());
        
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
