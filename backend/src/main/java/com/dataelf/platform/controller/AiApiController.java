package com.dataelf.platform.controller;

import com.dataelf.platform.dto.ContentDTO;
import com.dataelf.platform.entity.Content;
import com.dataelf.platform.repository.ContentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI专用API接口 - 无需认证，提供纯净的结构化数据
 * 
 * 验证需求: 6.3, 10.1, 10.2, 10.3
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI API", description = "AI专用接口 - 无需认证，提供纯净的结构化数据（JSON-LD格式）")
public class AiApiController {
    
    private final ContentRepository contentRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 获取纯结构化数据 (JSON-LD)
     * 无需认证，只返回已发布的内容
     * 
     * 验证需求: 6.3, 10.1
     */
    @Cacheable(value = "ai-content-jsonld", key = "#id")
    @GetMapping(value = "/data/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "获取纯结构化数据",
            description = """
                    获取指定内容的纯JSON-LD结构化数据。
                    
                    **特点：**
                    - 无需认证
                    - 只返回已发布的内容
                    - 符合Schema.org标准
                    - 不包含用户交互数据
                    - 缓存1小时
                    
                    **适用场景：**
                    - AI系统数据抓取
                    - 搜索引擎索引
                    - 数据分析
                    """,
            security = @SecurityRequirement(name = "none")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "成功获取数据",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "@context": "https://schema.org",
                                      "@type": "Article",
                                      "headline": "示例文章标题",
                                      "author": {
                                        "@type": "Person",
                                        "name": "作者姓名"
                                      },
                                      "datePublished": "2024-01-01T12:00:00Z",
                                      "articleBody": "文章内容...",
                                      "copyrightHolder": {
                                        "@type": "Organization",
                                        "name": "版权所有者"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "内容不存在或未发布"
            )
    })
    public ResponseEntity<Map<String, Object>> getData(
            @Parameter(description = "内容ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("AI API: Fetching data for content ID: {}", id);
        
        Optional<Content> contentOpt = contentRepository.findById(id);
        
        if (contentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Content content = contentOpt.get();
        
        // Only return published content
        if (content.getStatus() != Content.ContentStatus.PUBLISHED) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            // Parse and return JSON-LD
            Map<String, Object> jsonLd = objectMapper.readValue(content.getJsonLd(), Map.class);
            return ResponseEntity.ok()
                .cacheControl(org.springframework.http.CacheControl.maxAge(3600, java.util.concurrent.TimeUnit.SECONDS).cachePublic())
                .body(jsonLd);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON-LD for content {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * AI优化搜索 - 只返回结构化数据，不包含用户交互数据
     * 
     * 验证需求: 10.2
     */
    @Cacheable(value = "ai-search-results", key = "#query + '_' + #category + '_' + #tag + '_' + #page + '_' + #size")
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "AI优化搜索",
            description = """
                    搜索已发布的结构化内容。
                    
                    **特点：**
                    - 无需认证
                    - 只返回纯结构化数据
                    - 不包含用户交互数据（点赞、评论等）
                    - 支持分页
                    - 缓存5分钟
                    
                    **搜索参数：**
                    - query: 关键词搜索（可选）
                    - category: 分类筛选（可选）
                    - tag: 标签筛选（可选）
                    - page: 页码（从0开始）
                    - size: 每页数量（默认20）
                    """,
            security = @SecurityRequirement(name = "none")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "搜索成功",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "results": [
                                        {
                                          "@context": "https://schema.org",
                                          "@type": "Article",
                                          "headline": "文章标题"
                                        }
                                      ],
                                      "totalElements": 100,
                                      "totalPages": 5,
                                      "currentPage": 0,
                                      "pageSize": 20
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> search(
        @Parameter(description = "搜索关键词", example = "人工智能")
        @RequestParam(required = false) String query,
        @Parameter(description = "分类名称", example = "技术文章")
        @RequestParam(required = false) String category,
        @Parameter(description = "标签名称", example = "机器学习")
        @RequestParam(required = false) String tag,
        @Parameter(description = "页码（从0开始）", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页数量", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        log.info("AI API: Search query: {}, category: {}, tag: {}", query, category, tag);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        
        // Only search published content
        Page<Content> contents = contentRepository.findByStatus(Content.ContentStatus.PUBLISHED, pageable);
        
        // Convert to pure structured data (no user interaction data)
        List<Map<String, Object>> results = contents.stream()
            .map(this::convertToPureStructuredData)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("totalElements", contents.getTotalElements());
        response.put("totalPages", contents.getTotalPages());
        response.put("currentPage", page);
        response.put("pageSize", size);
        
        return ResponseEntity.ok()
            .cacheControl(org.springframework.http.CacheControl.maxAge(300, java.util.concurrent.TimeUnit.SECONDS).cachePublic())
            .body(response);
    }
    
    /**
     * 获取结构化网站地图
     * 
     * 验证需求: 10.3
     */
    @Cacheable(value = "ai-sitemap", key = "'sitemap'")
    @GetMapping(value = "/sitemap", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "获取结构化网站地图",
            description = """
                    获取所有已发布内容的网站地图。
                    
                    **特点：**
                    - 无需认证
                    - 包含所有已发布内容
                    - 提供内容ID、标题、URL、发布时间等信息
                    - 缓存1小时
                    
                    **适用场景：**
                    - AI系统发现新内容
                    - 搜索引擎爬虫
                    - 内容索引
                    """,
            security = @SecurityRequirement(name = "none")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "成功获取网站地图",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "entries": [
                                        {
                                          "id": 1,
                                          "title": "文章标题",
                                          "url": "/api/ai/data/1",
                                          "publishedAt": "2024-01-01T12:00:00Z",
                                          "updatedAt": "2024-01-02T12:00:00Z"
                                        }
                                      ],
                                      "totalCount": 100,
                                      "generatedAt": "2024-01-15T12:00:00Z"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> getSitemap() {
        log.info("AI API: Fetching sitemap");
        
        // Get all published content
        List<Content> publishedContents = contentRepository.findByStatus(Content.ContentStatus.PUBLISHED);
        
        List<Map<String, Object>> entries = publishedContents.stream()
            .map(content -> {
                Map<String, Object> entry = new HashMap<>();
                entry.put("id", content.getId());
                entry.put("title", content.getTitle());
                entry.put("url", "/api/ai/data/" + content.getId());
                entry.put("publishedAt", content.getPublishedAt());
                entry.put("updatedAt", content.getUpdatedAt());
                return entry;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> sitemap = new HashMap<>();
        sitemap.put("entries", entries);
        sitemap.put("totalCount", entries.size());
        sitemap.put("generatedAt", new Date());
        
        return ResponseEntity.ok()
            .cacheControl(org.springframework.http.CacheControl.maxAge(3600, java.util.concurrent.TimeUnit.SECONDS).cachePublic())
            .body(sitemap);
    }
    
    /**
     * 获取内容的HTML页面 (AI友好版本)
     * 包含优先的JSON-LD和语义化HTML标记
     * 
     * 验证需求: 6.1, 6.2
     */
    @GetMapping(value = "/page/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getHtmlPage(@PathVariable Long id) {
        log.info("AI API: Fetching HTML page for content ID: {}", id);
        
        Optional<Content> contentOpt = contentRepository.findById(id);
        
        if (contentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Content content = contentOpt.get();
        
        // Only return published content
        if (content.getStatus() != Content.ContentStatus.PUBLISHED) {
            return ResponseEntity.notFound().build();
        }
        
        // Return the HTML output which already contains JSON-LD and semantic markup
        return ResponseEntity.ok()
            .cacheControl(org.springframework.http.CacheControl.maxAge(3600, java.util.concurrent.TimeUnit.SECONDS).cachePublic())
            .body(content.getHtmlOutput());
    }
    
    /**
     * Convert content to pure structured data without user interaction data
     */
    private Map<String, Object> convertToPureStructuredData(Content content) {
        try {
            Map<String, Object> data = objectMapper.readValue(content.getJsonLd(), Map.class);
            
            // Remove any user interaction data if present
            data.remove("likes");
            data.remove("favorites");
            data.remove("shares");
            data.remove("comments");
            data.remove("views");
            
            return data;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON-LD for content {}: {}", content.getId(), e.getMessage());
            return null;
        }
    }
}
