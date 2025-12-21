package com.dataelf.platform.controller;

import com.dataelf.platform.dto.ApproveUserRequest;
import com.dataelf.platform.dto.ExtendAccountRequest;
import com.dataelf.platform.dto.RegisterRequest;
import com.dataelf.platform.entity.User;
import com.dataelf.platform.repository.UserRepository;
import com.dataelf.platform.service.UserService;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    private Long testUserId;
    private Long adminId = 999L;
    
    @BeforeEach
    void setUp() {
        // Create a test user
        RegisterRequest request = new RegisterRequest();
        request.setEmail("testuser@example.com");
        request.setPhone("13800138000");
        request.setPassword("Password123");
        
        testUserId = userService.register(request).getId();
    }
    
    @Test
    void testApproveUser_Success() throws Exception {
        // Given
        ApproveUserRequest request = new ApproveUserRequest();
        request.setUserId(testUserId);
        request.setValidDays(30);
        
        // When & Then
        mockMvc.perform(post("/api/admin/users/approve")
                .header("X-Admin-Id", adminId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("用户账号已批准"));
        
        // Verify user status
        User user = userRepository.findById(testUserId).orElseThrow();
        assertEquals(User.UserStatus.APPROVED, user.getStatus());
        assertNotNull(user.getApprovedAt());
        assertNotNull(user.getExpiresAt());
        assertEquals(adminId, user.getApprovedBy());
    }
    
    @Test
    void testApproveUser_WithoutValidDays() throws Exception {
        // Given
        ApproveUserRequest request = new ApproveUserRequest();
        request.setUserId(testUserId);
        request.setValidDays(null);
        
        // When & Then
        mockMvc.perform(post("/api/admin/users/approve")
                .header("X-Admin-Id", adminId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        // Verify user status
        User user = userRepository.findById(testUserId).orElseThrow();
        assertEquals(User.UserStatus.APPROVED, user.getStatus());
        assertNotNull(user.getApprovedAt());
        assertNull(user.getExpiresAt()); // No expiry set
    }
    
    @Test
    void testExtendAccount_Success() throws Exception {
        // Given - First approve the user
        userService.approveUser(testUserId, adminId, 5);
        
        ExtendAccountRequest request = new ExtendAccountRequest();
        request.setUserId(testUserId);
        request.setDays(30);
        
        // When & Then
        mockMvc.perform(post("/api/admin/users/extend")
                .header("X-Admin-Id", adminId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("账号时长已延长"));
        
        // Verify expiry date extended
        User user = userRepository.findById(testUserId).orElseThrow();
        assertNotNull(user.getExpiresAt());
        assertTrue(user.getExpiresAt().isAfter(LocalDateTime.now().plusDays(30)));
    }
    
    @Test
    void testGetUserDetails_Success() throws Exception {
        // Given - Approve user first
        userService.approveUser(testUserId, adminId, 30);
        
        // When & Then
        mockMvc.perform(get("/api/admin/users/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.id").value(testUserId))
                .andExpect(jsonPath("$.data.user.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.data.user.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.remainingDays").isNumber());
    }
    
    @Test
    void testGetPendingUsers_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/users/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[?(@.email == 'testuser@example.com')]").exists());
    }
    
    @Test
    void testGetExpiringUsers_Success() throws Exception {
        // Given - Create a user expiring in 5 days
        userService.approveUser(testUserId, adminId, 5);
        
        // When & Then
        mockMvc.perform(get("/api/admin/users/expiring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }
    
    @Test
    void testApproveUser_InvalidUserId() throws Exception {
        // Given
        ApproveUserRequest request = new ApproveUserRequest();
        request.setUserId(99999L); // Non-existent user
        request.setValidDays(30);
        
        // When & Then
        mockMvc.perform(post("/api/admin/users/approve")
                .header("X-Admin-Id", adminId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}
