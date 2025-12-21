package com.dataelf.platform.controller;

import com.dataelf.platform.dto.NotificationDTO;
import com.dataelf.platform.service.NotificationService;
import com.dataelf.platform.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "站内通知相关接口")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    
    /**
     * 获取当前用户的所有通知
     */
    @GetMapping
    @Operation(summary = "获取用户通知列表")
    public ResponseEntity<Map<String, Object>> getUserNotifications(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", notifications);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取当前用户的未读通知
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读通知")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", notifications);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读通知数量")
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        
        Long count = notificationService.getUnreadCount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        getUserIdFromToken(request); // 验证用户身份
        
        notificationService.markAsRead(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "通知已标记为已读");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    @Operation(summary = "标记所有通知为已读")
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        
        notificationService.markAllAsRead(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "所有通知已标记为已读");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 从请求中提取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("未找到有效的认证令牌");
    }
}
