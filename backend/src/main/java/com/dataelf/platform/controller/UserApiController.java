package com.dataelf.platform.controller;

import com.dataelf.platform.dto.*;
import com.dataelf.platform.entity.UserInteraction;
import com.dataelf.platform.service.CommentService;
import com.dataelf.platform.service.ContentService;
import com.dataelf.platform.service.UserInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "用户API", description = "需要JWT认证的用户接口 - 提供内容提交、个性化推荐和交互功能")
@SecurityRequirement(name = "bearerAuth")
public class UserApiController {
    
    private final ContentService contentService;
    private final UserInteractionService userInteractionService;
    private final CommentService commentService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @PostMapping("/submit")
    @Operation(
            summary = "提交内容",
            description = """
                    用户提交新的结构化内容。
                    
                    **提交流程：**
                    1. 选择内容模板
                    2. 填写结构化字段
                    3. 系统验证完整性
                    4. 创建内容（状态：DRAFT）
                    5. 可选：提交审核
                    
                    **需要认证：** JWT令牌
                    
                    **验证需求：** 3.1, 10.4
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "内容创建成功",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "内容创建成功",
                                      "data": {
                                        "id": 1,
                                        "title": "示例文章",
                                        "status": "DRAFT",
                                        "createdAt": "2024-01-01T12:00:00Z"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "验证失败"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "未认证"
            )
    })
    public ResponseEntity<Map<String, Object>> submitContent(
            @Valid @RequestBody ContentCreateRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} submitting content: {}", userId, request.getTitle());
        
        ContentDTO content = contentService.createContent(userId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "内容创建成功");
        response.put("data", content);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/feed")
    @Operation(
            summary = "获取个性化内容流",
            description = """
                    获取用户的个性化内容推荐。
                    
                    **特点：**
                    - 基于用户兴趣推荐
                    - 按发布时间排序
                    - 支持分页
                    - 包含完整内容信息
                    
                    **需要认证：** JWT令牌
                    
                    **验证需求：** 10.5
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "content": [...],
                                        "totalElements": 100,
                                        "totalPages": 5,
                                        "currentPage": 0
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "未认证"
            )
    })
    public ResponseEntity<Map<String, Object>> getFeed(
            @Parameter(description = "页码，从0开始", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} requesting feed, page: {}, size: {}", userId, page, size);
        
        // 创建分页请求，按发布时间降序排列
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        
        // 获取已发布的内容作为feed
        Page<ContentDTO> feed = contentService.getPublishedContents(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", feed.getContent());
        response.put("pagination", Map.of(
            "currentPage", feed.getNumber(),
            "totalPages", feed.getTotalPages(),
            "totalElements", feed.getTotalElements(),
            "size", feed.getSize()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/interact/{action}")
    @Operation(summary = "用户交互操作", description = "执行点赞、收藏、分享等交互操作（需认证）")
    public ResponseEntity<Map<String, Object>> interact(
            @Parameter(description = "交互类型：like, favorite, share") 
            @PathVariable String action,
            @Parameter(description = "内容ID") 
            @RequestParam Long contentId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} performing {} on content {}", userId, action, contentId);
        
        UserInteraction.InteractionType interactionType;
        try {
            interactionType = UserInteraction.InteractionType.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "无效的交互类型: " + action);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        userInteractionService.addInteraction(userId, contentId, interactionType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "操作成功");
        response.put("data", Map.of(
            "action", action,
            "contentId", contentId
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/favorites")
    @Operation(summary = "获取收藏列表", description = "获取用户收藏的内容列表（需认证）")
    public ResponseEntity<Map<String, Object>> getFavorites(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} requesting favorites, page: {}, size: {}", userId, page, size);
        
        List<ContentDTO> favorites = userInteractionService.getUserFavorites(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", favorites);
        response.put("total", favorites.size());
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/interact/{action}")
    @Operation(summary = "取消用户交互", description = "取消点赞、收藏等交互操作（需认证）")
    public ResponseEntity<Map<String, Object>> removeInteraction(
            @Parameter(description = "交互类型：like, favorite, share") 
            @PathVariable String action,
            @Parameter(description = "内容ID") 
            @RequestParam Long contentId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} removing {} from content {}", userId, action, contentId);
        
        UserInteraction.InteractionType interactionType;
        try {
            interactionType = UserInteraction.InteractionType.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "无效的交互类型: " + action);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        userInteractionService.removeInteraction(userId, contentId, interactionType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "操作成功");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/interact/status")
    @Operation(summary = "获取交互状态", description = "获取用户对某内容的交互状态（需认证）")
    public ResponseEntity<Map<String, Object>> getInteractionStatus(
            @Parameter(description = "内容ID") @RequestParam Long contentId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        
        boolean hasLiked = userInteractionService.hasInteraction(userId, contentId, UserInteraction.InteractionType.LIKE);
        boolean hasFavorited = userInteractionService.hasInteraction(userId, contentId, UserInteraction.InteractionType.FAVORITE);
        
        Long likeCount = userInteractionService.getInteractionCount(contentId, UserInteraction.InteractionType.LIKE);
        Long favoriteCount = userInteractionService.getInteractionCount(contentId, UserInteraction.InteractionType.FAVORITE);
        Long commentCount = commentService.getCommentCount(contentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of(
            "hasLiked", hasLiked,
            "hasFavorited", hasFavorited,
            "likeCount", likeCount,
            "favoriteCount", favoriteCount,
            "commentCount", commentCount
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/share")
    @Operation(summary = "生成分享链接", description = "生成带nofollow属性的分享链接（需认证）")
    public ResponseEntity<Map<String, Object>> generateShareLink(
            @Parameter(description = "内容ID") @RequestParam Long contentId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} generating share link for content {}", userId, contentId);
        
        // 验证内容是否存在
        ContentDTO content = contentService.getContent(contentId);
        if (content == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "内容不存在");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // 记录分享交互
        userInteractionService.addInteraction(userId, contentId, UserInteraction.InteractionType.SHARE);
        
        // 生成分享链接
        String shareUrl = baseUrl + "/content/" + contentId;
        String htmlLink = String.format("<a href=\"%s\" rel=\"nofollow\">%s</a>", shareUrl, content.getTitle());
        
        ShareLinkResponse shareLinkResponse = new ShareLinkResponse();
        shareLinkResponse.setContentUrl(shareUrl);
        shareLinkResponse.setTitle(content.getTitle());
        shareLinkResponse.setShareUrl(shareUrl);
        shareLinkResponse.setHtmlLink(htmlLink);
        shareLinkResponse.setContentId(contentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "分享链接生成成功");
        response.put("data", shareLinkResponse);
        
        return ResponseEntity.ok(response);
    }
    
    // 评论功能已移至 CommentController
    // POST /api/user/comments - 创建评论
    // GET /api/public/comments/content/{contentId} - 获取评论列表
    // GET /api/public/comments/content/{contentId}/count - 获取评论数量
}
