package com.dataelf.platform.controller;

import com.dataelf.platform.dto.*;
import com.dataelf.platform.service.ContentService;
import com.dataelf.platform.service.UserService;
import com.dataelf.platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@Slf4j
public class ContentController {
    
    private final ContentService contentService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createContent(
        @Valid @RequestBody ContentCreateRequest request,
        HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        // 检查VIP是否有效
        if (!userService.isAccountValid(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("code", "VIP_EXPIRED", "message", "您的VIP已过期，请续费后再创建内容"));
            return ResponseEntity.status(403).body(response);
        }
        
        ContentDTO content = contentService.createContent(userId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", content);
        response.put("message", "内容创建成功");
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateContent(
        @PathVariable Long id,
        @RequestBody ContentUpdateRequest request,
        HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        // 检查VIP是否有效
        if (!userService.isAccountValid(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("code", "VIP_EXPIRED", "message", "您的VIP已过期，请续费后再编辑内容"));
            return ResponseEntity.status(403).body(response);
        }
        
        // Verify ownership
        ContentDTO existingContent = contentService.getContent(id);
        if (!existingContent.getUserId().equals(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("message", "无权限修改此内容"));
            return ResponseEntity.status(403).body(response);
        }
        
        ContentDTO content = contentService.updateContent(id, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", content);
        response.put("message", "内容更新成功");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getContent(@PathVariable Long id) {
        ContentDTO content = contentService.getContent(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", content);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitForReview(
        @PathVariable Long id,
        HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        // 检查VIP是否有效
        if (!userService.isAccountValid(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("code", "VIP_EXPIRED", "message", "您的VIP已过期，请续费后再提交审核"));
            return ResponseEntity.status(403).body(response);
        }
        
        // Verify ownership
        ContentDTO content = contentService.getContent(id);
        if (!content.getUserId().equals(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("message", "无权限提交此内容"));
            return ResponseEntity.status(403).body(response);
        }
        
        contentService.submitForReview(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "内容已提交审核");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishContent(
        @PathVariable Long id,
        HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        // Verify ownership
        ContentDTO content = contentService.getContent(id);
        if (!content.getUserId().equals(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("message", "无权限发布此内容"));
            return ResponseEntity.status(403).body(response);
        }
        
        contentService.publishContent(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "内容已发布");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyContents(
        HttpServletRequest httpRequest,
        Pageable pageable
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        Page<ContentDTO> contents = contentService.getUserContents(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", contents);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my/drafts")
    public ResponseEntity<Map<String, Object>> getMyDrafts(
        HttpServletRequest httpRequest,
        Pageable pageable
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        Page<ContentDTO> drafts = contentService.getUserDrafts(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", drafts);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my/published")
    public ResponseEntity<Map<String, Object>> getMyPublishedContents(
        HttpServletRequest httpRequest,
        Pageable pageable
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        Page<ContentDTO> published = contentService.getUserPublishedContents(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", published);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my/pending")
    public ResponseEntity<Map<String, Object>> getMyPendingContents(
        HttpServletRequest httpRequest,
        Pageable pageable
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        Page<ContentDTO> pending = contentService.getUserPendingContents(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", pending);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/output")
    public ResponseEntity<Map<String, Object>> getOutput(@PathVariable Long id) {
        MultiFormatOutput output = contentService.generateOutput(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", output);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/copyright")
    public ResponseEntity<Map<String, Object>> updateCopyrightInfo(
        @PathVariable Long id,
        @RequestBody CopyrightInfoDTO copyrightInfo,
        HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        // Verify ownership
        ContentDTO content = contentService.getContent(id);
        if (!content.getUserId().equals(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("message", "无权限修改此内容"));
            return ResponseEntity.status(403).body(response);
        }
        
        contentService.updateCopyrightInfo(id, copyrightInfo);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "版权信息更新成功");
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/reorder")
    public ResponseEntity<Map<String, Object>> reorderFields(
        @PathVariable Long id,
        @RequestBody Map<String, List<String>> request,
        HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        // Verify ownership
        ContentDTO content = contentService.getContent(id);
        if (!content.getUserId().equals(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("message", "无权限修改此内容"));
            return ResponseEntity.status(403).body(response);
        }
        
        List<String> fieldOrder = request.get("fieldOrder");
        ContentDTO updatedContent = contentService.reorderFields(id, fieldOrder);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", updatedContent);
        response.put("message", "字段顺序更新成功");
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteContent(
        @PathVariable Long id,
        HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        
        // Verify ownership
        ContentDTO content = contentService.getContent(id);
        if (!content.getUserId().equals(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("message", "无权限删除此内容"));
            return ResponseEntity.status(403).body(response);
        }
        
        contentService.deleteContent(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "内容已删除");
        
        return ResponseEntity.ok(response);
    }
    
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("未提供认证令牌");
    }
}
