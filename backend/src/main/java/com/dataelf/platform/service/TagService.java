package com.dataelf.platform.service;

import com.dataelf.platform.dto.TagCreateRequest;
import com.dataelf.platform.dto.TagDTO;
import com.dataelf.platform.entity.Content;
import com.dataelf.platform.entity.Tag;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.ContentRepository;
import com.dataelf.platform.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {
    
    private final TagRepository tagRepository;
    private final ContentRepository contentRepository;
    
    /**
     * 创建标签
     */
    @Transactional
    public TagDTO createTag(TagCreateRequest request) {
        log.info("Creating tag: {}", request.getName());
        
        // 检查标签是否已存在
        if (tagRepository.findByName(request.getName()).isPresent()) {
            throw new ValidationException("标签已存在");
        }
        
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setUsageCount(0);
        
        tag = tagRepository.save(tag);
        log.info("Tag created with id: {}", tag.getId());
        
        return convertToDTO(tag);
    }
    
    /**
     * 获取所有标签
     */
    public List<TagDTO> getAllTags() {
        log.info("Getting all tags");
        List<Tag> tags = tagRepository.findAll();
        
        return tags.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取热门标签（按使用次数排序）
     */
    public List<TagDTO> getPopularTags() {
        log.info("Getting popular tags");
        List<Tag> tags = tagRepository.findAllByOrderByUsageCountDesc();
        
        return tags.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取指定标签
     */
    public TagDTO getTag(Long id) {
        log.info("Getting tag: {}", id);
        Tag tag = tagRepository.findById(id)
            .orElseThrow(() -> new ValidationException("标签不存在"));
        
        return convertToDTO(tag);
    }
    
    /**
     * 搜索标签
     */
    public List<TagDTO> searchTags(String keyword) {
        log.info("Searching tags with keyword: {}", keyword);
        List<Tag> tags = tagRepository.findByNameContaining(keyword);
        
        return tags.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 根据标签搜索内容
     */
    public Page<Content> searchContentsByTag(String tagName, Pageable pageable) {
        log.info("Searching contents by tag: {}", tagName);
        
        Tag tag = tagRepository.findByName(tagName)
            .orElseThrow(() -> new ValidationException("标签不存在"));
        
        // 查询包含该标签的已发布内容
        return contentRepository.findByTagsContainingAndStatus(tag, Content.ContentStatus.PUBLISHED, pageable);
    }
    
    /**
     * 删除标签
     */
    @Transactional
    public void deleteTag(Long id) {
        log.info("Deleting tag: {}", id);
        
        Tag tag = tagRepository.findById(id)
            .orElseThrow(() -> new ValidationException("标签不存在"));
        
        // 检查是否有内容使用该标签
        if (tag.getUsageCount() > 0) {
            throw new ValidationException("该标签正在被使用，无法删除");
        }
        
        tagRepository.delete(tag);
        log.info("Tag deleted: {}", id);
    }
    
    /**
     * 获取或创建标签
     */
    @Transactional
    public Tag getOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName)
            .orElseGet(() -> {
                Tag newTag = new Tag();
                newTag.setName(tagName);
                newTag.setUsageCount(0);
                return tagRepository.save(newTag);
            });
    }
    
    /**
     * 增加标签使用次数
     */
    @Transactional
    public void incrementUsageCount(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new ValidationException("标签不存在"));
        
        tag.setUsageCount(tag.getUsageCount() + 1);
        tagRepository.save(tag);
    }
    
    /**
     * 减少标签使用次数
     */
    @Transactional
    public void decrementUsageCount(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new ValidationException("标签不存在"));
        
        if (tag.getUsageCount() > 0) {
            tag.setUsageCount(tag.getUsageCount() - 1);
            tagRepository.save(tag);
        }
    }
    
    /**
     * 转换为DTO
     */
    private TagDTO convertToDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setUsageCount(tag.getUsageCount());
        dto.setCreatedAt(tag.getCreatedAt());
        
        return dto;
    }
}
