package com.dataelf.platform.service;

import com.dataelf.platform.dto.ContentDTO;
import com.dataelf.platform.entity.Content;
import com.dataelf.platform.entity.UserInteraction;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.ContentRepository;
import com.dataelf.platform.repository.UserInteractionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInteractionService {
    
    private final UserInteractionRepository userInteractionRepository;
    private final ContentRepository contentRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public void addInteraction(Long userId, Long contentId, UserInteraction.InteractionType interactionType) {
        // 验证内容是否存在
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        // 检查是否已经存在该交互
        Optional<UserInteraction> existing = userInteractionRepository
            .findByUserIdAndContentIdAndInteractionType(userId, contentId, interactionType);
        
        if (existing.isPresent()) {
            log.info("User {} already has {} interaction with content {}", userId, interactionType, contentId);
            return; // 已存在，不重复添加
        }
        
        // 创建新的交互记录
        UserInteraction interaction = new UserInteraction();
        interaction.setUserId(userId);
        interaction.setContentId(contentId);
        interaction.setInteractionType(interactionType);
        
        userInteractionRepository.save(interaction);
        log.info("Added {} interaction for user {} on content {}", interactionType, userId, contentId);
    }
    
    @Transactional
    public void removeInteraction(Long userId, Long contentId, UserInteraction.InteractionType interactionType) {
        Optional<UserInteraction> interaction = userInteractionRepository
            .findByUserIdAndContentIdAndInteractionType(userId, contentId, interactionType);
        
        interaction.ifPresent(i -> {
            userInteractionRepository.delete(i);
            log.info("Removed {} interaction for user {} on content {}", interactionType, userId, contentId);
        });
    }
    
    @Transactional(readOnly = true)
    public List<ContentDTO> getUserFavorites(Long userId) {
        List<UserInteraction> favorites = userInteractionRepository
            .findByUserIdAndInteractionType(userId, UserInteraction.InteractionType.FAVORITE);
        
        List<Long> contentIds = favorites.stream()
            .map(UserInteraction::getContentId)
            .collect(Collectors.toList());
        
        if (contentIds.isEmpty()) {
            return List.of();
        }
        
        List<Content> contents = contentRepository.findAllById(contentIds);
        
        return contents.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long getInteractionCount(Long contentId, UserInteraction.InteractionType interactionType) {
        return userInteractionRepository.countByContentIdAndInteractionType(contentId, interactionType);
    }
    
    @Transactional(readOnly = true)
    public boolean hasInteraction(Long userId, Long contentId, UserInteraction.InteractionType interactionType) {
        return userInteractionRepository
            .findByUserIdAndContentIdAndInteractionType(userId, contentId, interactionType)
            .isPresent();
    }
    
    private ContentDTO convertToDTO(Content content) {
        ContentDTO dto = new ContentDTO();
        dto.setId(content.getId());
        dto.setUserId(content.getUserId());
        dto.setTemplateId(content.getTemplateId());
        dto.setTitle(content.getTitle());
        dto.setStructuredData(parseStructuredData(content.getStructuredData()));
        dto.setJsonLd(content.getJsonLd());
        dto.setHtmlOutput(content.getHtmlOutput());
        dto.setMarkdownOutput(content.getMarkdownOutput());
        dto.setCopyrightNotice(content.getCopyrightNotice());
        dto.setContentSource(content.getContentSource());
        dto.setAuthorName(content.getAuthorName());
        dto.setIsOriginal(content.getIsOriginal());
        dto.setStatus(content.getStatus());
        dto.setIntegrityScore(content.getIntegrityScore());
        dto.setCreatedAt(content.getCreatedAt());
        dto.setUpdatedAt(content.getUpdatedAt());
        dto.setSubmittedAt(content.getSubmittedAt());
        dto.setReviewedAt(content.getReviewedAt());
        dto.setPublishedAt(content.getPublishedAt());
        dto.setReviewedBy(content.getReviewedBy());
        dto.setRejectReason(content.getRejectReason());
        return dto;
    }
    
    private Map<String, Object> parseStructuredData(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse structured data: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
