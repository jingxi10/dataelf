package com.dataelf.platform.controller;

import com.dataelf.platform.dto.ContentDTO;
import com.dataelf.platform.dto.TagCreateRequest;
import com.dataelf.platform.dto.TagDTO;
import com.dataelf.platform.entity.Content;
import com.dataelf.platform.service.ContentService;
import com.dataelf.platform.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {
    
    private final TagService tagService;
    private final ContentService contentService;
    
    /**
     * 创建标签（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createTag(@Valid @RequestBody TagCreateRequest request) {
        log.info("Creating tag: {}", request.getName());
        
        TagDTO tag = tagService.createTag(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tag);
        response.put("message", "标签创建成功");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 获取所有标签
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTags() {
        log.info("Getting all tags");
        
        List<TagDTO> tags = tagService.getAllTags();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tags);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取热门标签
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularTags() {
        log.info("Getting popular tags");
        
        List<TagDTO> tags = tagService.getPopularTags();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tags);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取指定标签
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTag(@PathVariable Long id) {
        log.info("Getting tag: {}", id);
        
        TagDTO tag = tagService.getTag(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tag);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTags(@RequestParam String keyword) {
        log.info("Searching tags with keyword: {}", keyword);
        
        List<TagDTO> tags = tagService.searchTags(keyword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tags);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据标签搜索内容
     */
    @GetMapping("/{tagName}/contents")
    public ResponseEntity<Map<String, Object>> searchContentsByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Searching contents by tag: {}", tagName);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Content> contentPage = tagService.searchContentsByTag(tagName, pageable);
        
        List<ContentDTO> contents = contentPage.getContent().stream()
            .map(contentService::convertToDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", contents);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", contentPage.getTotalElements());
        response.put("totalPages", contentPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除标签（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTag(@PathVariable Long id) {
        log.info("Deleting tag: {}", id);
        
        tagService.deleteTag(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "标签删除成功");
        
        return ResponseEntity.ok(response);
    }
}
