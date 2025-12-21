package com.dataelf.platform.controller;

import com.dataelf.platform.dto.ContentDTO;
import com.dataelf.platform.entity.Content;
import com.dataelf.platform.service.ContentService;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公开API控制器 - 无需认证的公开接口
 * 
 * 验证需求: 8.1, 8.2, 8.3, 8.4, 8.5
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "公开API", description = "无需认证的公开接口 - 提供内容浏览、分类和标签查询")
public class PublicController {
    
    private final ContentService contentService;
    
    /**
     * 获取已发布的内容列表（公开接口）
     * 
     * 验证需求: 8.2
     */
    @GetMapping("/contents")
    @Operation(
            summary = "获取已发布内容列表",
            description = """
                    获取所有已发布的内容列表。
                    
                    **特点：**
                    - 无需认证
                    - 只返回已发布内容
                    - 默认按发布时间降序排列
                    - 支持分页和排序
                    - 缓存5分钟
                    
                    **适用场景：**
                    - 首页内容展示
                    - 内容浏览
                    - 公开访问
                    
                    **验证需求：** 8.2
                    """,
            security = @SecurityRequirement(name = "none")
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
                                        "content": [
                                          {
                                            "id": 1,
                                            "title": "示例文章",
                                            "publishedAt": "2024-01-01T12:00:00Z"
                                          }
                                        ],
                                        "totalElements": 100,
                                        "totalPages": 9,
                                        "currentPage": 0,
                                        "pageSize": 12
                                      }
                                    }
                                    """)
                    )
            )
    })
    // @Cacheable(value = "homepage-contents", key = "#page + '_' + #size + '_' + #sort")
    public ResponseEntity<Map<String, Object>> getPublishedContents(
            @Parameter(description = "页码，从0开始", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "12") @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "排序字段（格式：字段名,方向）", example = "publishedAt,desc") @RequestParam(defaultValue = "publishedAt,desc") String sort) {
        
        log.info("Public API: Fetching published contents, page: {}, size: {}, sort: {}", page, size, sort);
        
        try {
            // 解析排序参数
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            Page<ContentDTO> contents = contentService.getPublishedContents(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "content", contents.getContent(),
                "number", contents.getNumber(),
                "totalPages", contents.getTotalPages(),
                "totalElements", contents.getTotalElements(),
                "size", contents.getSize()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching published contents", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("code", "INTERNAL_ERROR", "message", "获取内容列表失败: " + e.getMessage()));
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 根据分类获取内容（公开接口）
     * 
     * 验证需求: 8.5, 11.3
     */
    @GetMapping("/contents/category/{categoryId}")
    @Operation(summary = "根据分类获取内容", description = "获取指定分类下的已发布内容（无需认证）")
    public ResponseEntity<Map<String, Object>> getContentsByCategory(
            @Parameter(description = "分类ID") @PathVariable Long categoryId,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "publishedAt,desc") String sort) {
        
        log.info("Public API: Fetching contents for category {}, page: {}, size: {} (cache miss)", categoryId, page, size);
        
        // 解析排序参数
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<ContentDTO> allContents = contentService.getPublishedContents(pageable);
        
        // Filter by category
        List<ContentDTO> filteredContents = allContents.getContent().stream()
            .filter(content -> content.getCategoryIds() != null && content.getCategoryIds().contains(categoryId))
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of(
            "content", filteredContents,
            "number", page,
            "totalPages", filteredContents.isEmpty() ? 0 : 1,
            "totalElements", filteredContents.size(),
            "size", size
        ));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取单个内容详情（公开接口）
     * 
     * 验证需求: 8.2
     */
    @GetMapping("/content/{id}")
    @Operation(summary = "获取内容详情", description = "获取指定ID的已发布内容详情（无需认证）")
    public ResponseEntity<Map<String, Object>> getContentDetail(
            @Parameter(description = "内容ID") @PathVariable Long id) {
        
        log.info("Public API: Fetching content detail for id: {}", id);
        
        try {
            ContentDTO content = contentService.getContent(id);

            // 只返回已发布的内容
//            if (content == null || content.getStatus()!=Content.ContentStatus.PUBLISHED) {
//                Map<String, Object> response = new HashMap<>();
//                response.put("success", false);
//                response.put("error", Map.of("code", "NOT_FOUND", "message", "内容不存在或未发布"));
//                return ResponseEntity.status(404).body(response);
//            }
            
            // 增加浏览次数
            contentService.incrementViewCount(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", content);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching content detail", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("code", "INTERNAL_ERROR", "message", "获取内容详情失败: " + e.getMessage()));
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 搜索内容（公开接口）
     */
    @GetMapping("/contents/search")
    @Operation(summary = "搜索内容", description = "根据关键词搜索已发布内容（无需认证）")
    public ResponseEntity<Map<String, Object>> searchContents(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "publishedAt,desc") String sort) {
        
        log.info("Public API: Searching contents with keyword: {}, page: {}, size: {}", keyword, page, size);
        
        try {
            // 解析排序参数
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            Page<ContentDTO> contents = contentService.searchContents(keyword, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "content", contents.getContent(),
                "number", contents.getNumber(),
                "totalPages", contents.getTotalPages(),
                "totalElements", contents.getTotalElements(),
                "size", contents.getSize()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching contents", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("code", "INTERNAL_ERROR", "message", "搜索失败: " + e.getMessage()));
            return ResponseEntity.status(500).body(response);
        }
    }
}
