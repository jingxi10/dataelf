package com.dataelf.platform.controller;

import com.dataelf.platform.dto.CommentCreateRequest;
import com.dataelf.platform.dto.CommentDTO;
import com.dataelf.platform.service.CommentService;
import com.dataelf.platform.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "评论", description = "评论管理接口")
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    @PostMapping("/user/comments")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "创建评论", description = "用户发表评论")
    public ResponseEntity<Map<String, Object>> createComment(
            @Valid @RequestBody CommentCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        CommentDTO comment = commentService.createComment(userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", comment);
        response.put("message", "评论发表成功");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/comments/content/{contentId}")
    @Operation(summary = "获取内容评论", description = "获取指定内容的评论列表（公开）")
    public ResponseEntity<Map<String, Object>> getCommentsByContent(
            @PathVariable Long contentId,
            Pageable pageable
    ) {
        Page<CommentDTO> comments = commentService.getCommentsByContentId(contentId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", comments);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/comments/content/{contentId}/count")
    @Operation(summary = "获取评论数量", description = "获取指定内容的评论数量（公开）")
    public ResponseEntity<Map<String, Object>> getCommentCount(
            @PathVariable Long contentId
    ) {
        Long count = commentService.getCommentCount(contentId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", count);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/comments/{commentId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "删除评论", description = "用户或管理员删除评论")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long commentId,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromToken(httpRequest);
        boolean isAdmin = isAdminUser(httpRequest);

        commentService.deleteComment(commentId, userId, isAdmin);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "评论已删除");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/comments/{commentId}/pin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "置顶/取消置顶评论", description = "管理员置顶或取消置顶评论")
    public ResponseEntity<Map<String, Object>> togglePinComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Boolean> request,
            HttpServletRequest httpRequest
    ) {
        // 检查是否是管理员
        if (!isAdminUser(httpRequest)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "无权限操作");
            return ResponseEntity.status(403).body(response);
        }

        boolean pin = request != null && request.getOrDefault("pin", false);
        commentService.togglePinComment(commentId, pin);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", pin ? "评论已置顶" : "评论已取消置顶");

        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未找到有效的认证令牌");
    }

    private boolean isAdminUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String role = jwtUtil.getRoleFromToken(token);
            return "ADMIN".equals(role);
        }
        return false;
    }
}
