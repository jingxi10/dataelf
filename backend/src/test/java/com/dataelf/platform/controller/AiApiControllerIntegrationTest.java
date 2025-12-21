package com.dataelf.platform.controller;

import com.dataelf.platform.entity.Content;
import com.dataelf.platform.entity.Template;
import com.dataelf.platform.entity.User;
import com.dataelf.platform.repository.ContentRepository;
import com.dataelf.platform.repository.TemplateRepository;
import com.dataelf.platform.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AI API集成测试
 * 
 * 验证需求: 6.3, 10.1, 10.2, 10.3
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AiApiControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ContentRepository contentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Content publishedContent;
    private Content draftContent;
    
    @BeforeEach
    void setUp() {
        // 创建测试用户
        User user = new User();
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setPasswordHash("hashedpassword");
        user.setStatus(User.UserStatus.APPROVED);
        user = userRepository.save(user);
        
        // 创建测试模板
        Template template = new Template();
        template.setName("Test Template");
        template.setType("Article");
        template.setSchemaOrgType("Article");
        template.setSchemaDefinition("{}");
        template.setIsSystem(true);
        template = templateRepository.save(template);
        
        // 创建已发布内容
        Map<String, Object> jsonLdData = new HashMap<>();
        jsonLdData.put("@context", "https://schema.org");
        jsonLdData.put("@type", "Article");
        jsonLdData.put("headline", "Test Article");
        jsonLdData.put("author", Map.of("@type", "Person", "name", "Test Author"));
        
        publishedContent = new Content();
        publishedContent.setUserId(user.getId());
        publishedContent.setTemplateId(template.getId());
        publishedContent.setTitle("Test Article");
        publishedContent.setStructuredData("{}");
        try {
            publishedContent.setJsonLd(objectMapper.writeValueAsString(jsonLdData));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        publishedContent.setHtmlOutput("<html><body>Test</body></html>");
        publishedContent.setStatus(Content.ContentStatus.PUBLISHED);
        publishedContent.setPublishedAt(LocalDateTime.now());
        publishedContent = contentRepository.save(publishedContent);
        
        // 创建草稿内容
        draftContent = new Content();
        draftContent.setUserId(user.getId());
        draftContent.setTemplateId(template.getId());
        draftContent.setTitle("Draft Article");
        draftContent.setStructuredData("{}");
        draftContent.setJsonLd("{}");
        draftContent.setHtmlOutput("<html><body>Draft</body></html>");
        draftContent.setStatus(Content.ContentStatus.DRAFT);
        draftContent = contentRepository.save(draftContent);
    }
    
    /**
     * 测试获取已发布内容的JSON-LD数据
     * 验证需求: 6.3, 10.1
     * 属性 21: AI API无认证访问
     */
    @Test
    void testGetData_PublishedContent_ReturnsJsonLd() throws Exception {
        mockMvc.perform(get("/api/ai/data/{id}", publishedContent.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.@context").value("https://schema.org"))
                .andExpect(jsonPath("$.@type").value("Article"))
                .andExpect(jsonPath("$.headline").value("Test Article"))
                .andExpect(jsonPath("$.author.name").value("Test Author"))
                .andExpect(header().exists("Cache-Control"))
                .andExpect(header().string("Cache-Control", containsString("max-age=3600")));
    }
    
    /**
     * 测试获取未发布内容返回404
     * 验证需求: 6.3, 10.1
     */
    @Test
    void testGetData_DraftContent_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/ai/data/{id}", draftContent.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    /**
     * 测试获取不存在的内容返回404
     * 验证需求: 6.3, 10.1
     */
    @Test
    void testGetData_NonExistentContent_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/ai/data/{id}", 99999L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    /**
     * 测试AI搜索接口返回纯净的结构化数据
     * 验证需求: 10.2
     * 属性 35: AI搜索结果纯净性
     */
    @Test
    void testSearch_ReturnsPublishedContentOnly() throws Exception {
        mockMvc.perform(get("/api/ai/search")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(header().exists("Cache-Control"))
                .andExpect(header().string("Cache-Control", containsString("max-age=300")));
    }
    
    /**
     * 测试AI搜索结果不包含用户交互数据
     * 验证需求: 10.2
     * 属性 35: AI搜索结果纯净性
     */
    @Test
    void testSearch_ResultsDoNotContainUserInteractionData() throws Exception {
        mockMvc.perform(get("/api/ai/search")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[*].likes").doesNotExist())
                .andExpect(jsonPath("$.results[*].favorites").doesNotExist())
                .andExpect(jsonPath("$.results[*].shares").doesNotExist())
                .andExpect(jsonPath("$.results[*].comments").doesNotExist())
                .andExpect(jsonPath("$.results[*].views").doesNotExist());
    }
    
    /**
     * 测试AI搜索支持分页
     * 验证需求: 10.2
     */
    @Test
    void testSearch_SupportsPagination() throws Exception {
        mockMvc.perform(get("/api/ai/search")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }
    
    /**
     * 测试获取网站地图
     * 验证需求: 10.3
     * 属性 36: Sitemap内容完整性
     */
    @Test
    void testGetSitemap_ReturnsAllPublishedContent() throws Exception {
        mockMvc.perform(get("/api/ai/sitemap")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.entries").isArray())
                .andExpect(jsonPath("$.entries", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.totalCount").isNumber())
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(header().exists("Cache-Control"))
                .andExpect(header().string("Cache-Control", containsString("max-age=3600")));
    }
    
    /**
     * 测试网站地图包含必要的字段
     * 验证需求: 10.3
     * 属性 36: Sitemap内容完整性
     */
    @Test
    void testGetSitemap_EntriesContainRequiredFields() throws Exception {
        mockMvc.perform(get("/api/ai/sitemap")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].id").exists())
                .andExpect(jsonPath("$.entries[0].title").exists())
                .andExpect(jsonPath("$.entries[0].url").exists())
                .andExpect(jsonPath("$.entries[0].publishedAt").exists())
                .andExpect(jsonPath("$.entries[0].updatedAt").exists());
    }
    
    /**
     * 测试获取HTML页面
     * 验证需求: 6.1, 6.2
     */
    @Test
    void testGetHtmlPage_ReturnsHtmlContent() throws Exception {
        mockMvc.perform(get("/api/ai/page/{id}", publishedContent.getId())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("<html>")))
                .andExpect(header().exists("Cache-Control"))
                .andExpect(header().string("Cache-Control", containsString("max-age=3600")));
    }
    
    /**
     * 测试AI API端点无需认证即可访问
     * 验证需求: 6.3, 10.1
     * 属性 21: AI API无认证访问
     */
    @Test
    void testAiApiEndpoints_NoAuthenticationRequired() throws Exception {
        // 所有AI API端点都应该无需认证即可访问
        mockMvc.perform(get("/api/ai/data/{id}", publishedContent.getId()))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/ai/search"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/ai/sitemap"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/ai/page/{id}", publishedContent.getId()))
                .andExpect(status().isOk());
    }
}
