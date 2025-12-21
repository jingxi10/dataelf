package com.dataelf.platform.controller;

import com.dataelf.platform.dto.ContentCreateRequest;
import com.dataelf.platform.dto.LoginRequest;
import com.dataelf.platform.dto.LoginResponse;
import com.dataelf.platform.dto.RegisterRequest;
import com.dataelf.platform.entity.Content;
import com.dataelf.platform.entity.Template;
import com.dataelf.platform.entity.User;
import com.dataelf.platform.entity.UserInteraction;
import com.dataelf.platform.repository.*;
import com.dataelf.platform.service.UserService;
import com.dataelf.platform.util.JwtUtil;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserApiControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private ContentRepository contentRepository;
    
    @Autowired
    private UserInteractionRepository userInteractionRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    private String userToken;
    private Long userId;
    private Long templateId;
    
    @BeforeEach
    void setUp() {
        // Clean up
        userInteractionRepository.deleteAll();
        contentRepository.deleteAll();
        templateRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create and approve a test user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("testuser@example.com");
        registerRequest.setPhone("13800138000");
        registerRequest.setPassword("TestPass123");
        
        userService.register(registerRequest);
        
        User user = userRepository.findByEmail("testuser@example.com").orElseThrow();
        user.setStatus(User.UserStatus.APPROVED);
        user.setExpiresAt(LocalDateTime.now().plusDays(30));
        userRepository.save(user);
        
        userId = user.getId();
        
        // Login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("testuser@example.com");
        loginRequest.setPassword("TestPass123");
        
        LoginResponse loginResponse = userService.login(loginRequest);
        userToken = loginResponse.getAccessToken();
        
        // Create a test template
        Template template = new Template();
        template.setName("测试模板");
        template.setType("Article");
        template.setDescription("测试用模板");
        template.setSchemaOrgType("Article");
        
        Map<String, Object> schemaDefinition = new HashMap<>();
        schemaDefinition.put("fields", new Object[]{
            Map.of("name", "title", "type", "string", "required", true),
            Map.of("name", "content", "type", "text", "required", true)
        });
        
        try {
            template.setSchemaDefinition(objectMapper.writeValueAsString(schemaDefinition));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        template.setIsSystem(false);
        template = templateRepository.save(template);
        templateId = template.getId();
    }
    
    @Test
    void testSubmitContent_WithAuthentication_Success() throws Exception {
        ContentCreateRequest request = new ContentCreateRequest();
        request.setTemplateId(templateId);
        request.setTitle("测试内容");
        
        Map<String, Object> structuredData = new HashMap<>();
        structuredData.put("title", "测试内容");
        structuredData.put("content", "这是测试内容");
        request.setStructuredData(structuredData);
        
        mockMvc.perform(post("/api/user/submit")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("内容创建成功"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value("测试内容"));
    }
    
    @Test
    void testSubmitContent_WithoutAuthentication_Unauthorized() throws Exception {
        ContentCreateRequest request = new ContentCreateRequest();
        request.setTemplateId(templateId);
        request.setTitle("测试内容");
        
        Map<String, Object> structuredData = new HashMap<>();
        structuredData.put("title", "测试内容");
        structuredData.put("content", "这是测试内容");
        request.setStructuredData(structuredData);
        
        mockMvc.perform(post("/api/user/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testGetFeed_WithAuthentication_Success() throws Exception {
        // Create and publish some content
        Content content = new Content();
        content.setUserId(userId);
        content.setTemplateId(templateId);
        content.setTitle("已发布内容");
        content.setStructuredData("{\"title\":\"已发布内容\",\"content\":\"内容\"}");
        content.setJsonLd("{}");
        content.setHtmlOutput("<html></html>");
        content.setMarkdownOutput("# 已发布内容");
        content.setStatus(Content.ContentStatus.PUBLISHED);
        content.setPublishedAt(LocalDateTime.now());
        contentRepository.save(content);
        
        mockMvc.perform(get("/api/user/feed")
                .header("Authorization", "Bearer " + userToken)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("已发布内容"))
                .andExpect(jsonPath("$.pagination.currentPage").value(0))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }
    
    @Test
    void testGetFeed_WithoutAuthentication_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/feed")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testInteract_Like_WithAuthentication_Success() throws Exception {
        // Create published content
        Content content = new Content();
        content.setUserId(userId);
        content.setTemplateId(templateId);
        content.setTitle("测试内容");
        content.setStructuredData("{\"title\":\"测试内容\"}");
        content.setJsonLd("{}");
        content.setHtmlOutput("<html></html>");
        content.setMarkdownOutput("# 测试内容");
        content.setStatus(Content.ContentStatus.PUBLISHED);
        content.setPublishedAt(LocalDateTime.now());
        content = contentRepository.save(content);
        
        mockMvc.perform(post("/api/user/interact/like")
                .header("Authorization", "Bearer " + userToken)
                .param("contentId", content.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.action").value("like"))
                .andExpect(jsonPath("$.data.contentId").value(content.getId()));
    }
    
    @Test
    void testInteract_Favorite_WithAuthentication_Success() throws Exception {
        // Create published content
        Content content = new Content();
        content.setUserId(userId);
        content.setTemplateId(templateId);
        content.setTitle("测试内容");
        content.setStructuredData("{\"title\":\"测试内容\"}");
        content.setJsonLd("{}");
        content.setHtmlOutput("<html></html>");
        content.setMarkdownOutput("# 测试内容");
        content.setStatus(Content.ContentStatus.PUBLISHED);
        content.setPublishedAt(LocalDateTime.now());
        content = contentRepository.save(content);
        
        mockMvc.perform(post("/api/user/interact/favorite")
                .header("Authorization", "Bearer " + userToken)
                .param("contentId", content.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.action").value("favorite"));
    }
    
    @Test
    void testInteract_InvalidAction_BadRequest() throws Exception {
        Content content = new Content();
        content.setUserId(userId);
        content.setTemplateId(templateId);
        content.setTitle("测试内容");
        content.setStructuredData("{\"title\":\"测试内容\"}");
        content.setJsonLd("{}");
        content.setHtmlOutput("<html></html>");
        content.setMarkdownOutput("# 测试内容");
        content.setStatus(Content.ContentStatus.PUBLISHED);
        content = contentRepository.save(content);
        
        mockMvc.perform(post("/api/user/interact/invalid")
                .header("Authorization", "Bearer " + userToken)
                .param("contentId", content.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("无效的交互类型")));
    }
    
    @Test
    void testInteract_WithoutAuthentication_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/user/interact/like")
                .param("contentId", "1"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testGetFavorites_WithAuthentication_Success() throws Exception {
        // Create published content
        Content content = new Content();
        content.setUserId(userId);
        content.setTemplateId(templateId);
        content.setTitle("收藏的内容");
        content.setStructuredData("{\"title\":\"收藏的内容\"}");
        content.setJsonLd("{}");
        content.setHtmlOutput("<html></html>");
        content.setMarkdownOutput("# 收藏的内容");
        content.setStatus(Content.ContentStatus.PUBLISHED);
        content = contentRepository.save(content);
        
        // Add favorite interaction
        UserInteraction interaction = new UserInteraction();
        interaction.setUserId(userId);
        interaction.setContentId(content.getId());
        interaction.setInteractionType(UserInteraction.InteractionType.FAVORITE);
        userInteractionRepository.save(interaction);
        
        mockMvc.perform(get("/api/user/favorites")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("收藏的内容"))
                .andExpect(jsonPath("$.total").value(1));
    }
    
    @Test
    void testGetFavorites_WithoutAuthentication_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/favorites"))
                .andExpect(status().isForbidden());
    }
}
