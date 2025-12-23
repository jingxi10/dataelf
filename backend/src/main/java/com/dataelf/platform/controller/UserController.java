package com.dataelf.platform.controller;

import com.dataelf.platform.dto.ContentDTO;
import com.dataelf.platform.dto.UpdateNicknameRequest;
import com.dataelf.platform.dto.UserDTO;
import com.dataelf.platform.entity.Content;
import com.dataelf.platform.entity.User;
import com.dataelf.platform.repository.ContentRepository;
import com.dataelf.platform.repository.UserRepository;
import com.dataelf.platform.service.ContentService;
import com.dataelf.platform.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "用户中心", description = "用户个人信息和内容管理接口")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final ContentService contentService;
    private final JwtUtil jwtUtil;
    
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未找到有效的认证令牌");
    }
    
    @GetMapping("/profile")
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的详细信息")
    public ResponseEntity<Map<String, Object>> getProfile(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", UserDTO.fromEntity(user));
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/nickname")
    @Operation(summary = "更新昵称", description = "更新当前用户的昵称")
    public ResponseEntity<Map<String, Object>> updateNickname(
            @Valid @RequestBody UpdateNicknameRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        log.info("User {} updating nickname to: {}", userId, request.getNickname());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setNickname(request.getNickname());
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "昵称更新成功");
        response.put("data", UserDTO.fromEntity(user));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/contents")
    @Operation(summary = "获取我的内容列表", description = "获取当前用户的所有内容，支持按状态筛选")
    public ResponseEntity<Map<String, Object>> getMyContents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("User {} fetching contents, page: {}, size: {}, status: {}", userId, page, size, status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Content> contentPage;
        if (status != null && !status.isEmpty()) {
            Content.ContentStatus contentStatus = Content.ContentStatus.valueOf(status);
            contentPage = contentRepository.findByUserIdAndStatus(userId, contentStatus, pageable);
        } else {
            contentPage = contentRepository.findByUserId(userId, pageable);
        }
        
        List<ContentDTO> contentDTOs = contentPage.getContent().stream()
                .map(contentService::convertToDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of(
            "content", contentDTOs,
            "totalElements", contentPage.getTotalElements(),
            "totalPages", contentPage.getTotalPages(),
            "currentPage", contentPage.getNumber(),
            "size", contentPage.getSize()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/contents/stats")
    @Operation(summary = "获取内容统计", description = "获取当前用户的内容统计信息")
    public ResponseEntity<Map<String, Object>> getContentStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        long total = contentRepository.countByUserId(userId);
        long draft = contentRepository.countByUserIdAndStatus(userId, Content.ContentStatus.DRAFT);
        long pending = contentRepository.countByUserIdAndStatus(userId, Content.ContentStatus.PENDING_REVIEW);
        long published = contentRepository.countByUserIdAndStatus(userId, Content.ContentStatus.PUBLISHED);
        long rejected = contentRepository.countByUserIdAndStatus(userId, Content.ContentStatus.REJECTED);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("draft", draft);
        stats.put("pending", pending);
        stats.put("published", published);
        stats.put("rejected", rejected);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        
        return ResponseEntity.ok(response);
    }
}
