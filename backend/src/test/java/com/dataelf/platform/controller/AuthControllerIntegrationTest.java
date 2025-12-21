package com.dataelf.platform.controller;

import com.dataelf.platform.dto.LoginRequest;
import com.dataelf.platform.dto.RegisterRequest;
import com.dataelf.platform.entity.User;
import com.dataelf.platform.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    
    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPhone("13800138000");
        request.setPassword("Password123");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("注册成功，等待审核，请留意查看邮箱"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
    
    @Test
    void testRegister_InvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPhone("13800138000");
        request.setPassword("Password123");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[*].field", hasItem("email")));
    }
    
    @Test
    void testRegister_InvalidPhone() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPhone("12345");
        request.setPassword("Password123");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[*].field", hasItem("phone")));
    }
    
    @Test
    void testRegister_WeakPassword() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPhone("13800138000");
        request.setPassword("weak");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[*].field", hasItem("password")));
    }
    
    @Test
    void testRegister_DuplicateEmail() throws Exception {
        // Create existing user
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setPhone("13800138000");
        existingUser.setPasswordHash(passwordEncoder.encode("Password123"));
        existingUser.setStatus(User.UserStatus.PENDING);
        userRepository.save(existingUser);
        
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPhone("13800138001");
        request.setPassword("Password123");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("邮箱已被注册"));
    }
    
    @Test
    void testLogin_Success() throws Exception {
        // Create approved user
        User user = new User();
        user.setEmail("approved@example.com");
        user.setPhone("13800138000");
        user.setPasswordHash(passwordEncoder.encode("Password123"));
        user.setStatus(User.UserStatus.APPROVED);
        user.setExpiresAt(LocalDateTime.now().plusDays(30));
        userRepository.save(user);
        
        LoginRequest request = new LoginRequest();
        request.setEmail("approved@example.com");
        request.setPassword("Password123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.email").value("approved@example.com"));
    }
    
    @Test
    void testLogin_InvalidPassword() throws Exception {
        // Create approved user
        User user = new User();
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setPasswordHash(passwordEncoder.encode("Password123"));
        user.setStatus(User.UserStatus.APPROVED);
        user.setExpiresAt(LocalDateTime.now().plusDays(30));
        userRepository.save(user);
        
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("邮箱或密码错误"));
    }
    
    @Test
    void testLogin_PendingAccount() throws Exception {
        // Create pending user
        User user = new User();
        user.setEmail("pending@example.com");
        user.setPhone("13800138000");
        user.setPasswordHash(passwordEncoder.encode("Password123"));
        user.setStatus(User.UserStatus.PENDING);
        userRepository.save(user);
        
        LoginRequest request = new LoginRequest();
        request.setEmail("pending@example.com");
        request.setPassword("Password123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("账号审核中"));
    }
    
    @Test
    void testLogin_ExpiredAccount() throws Exception {
        // Create expired user
        User user = new User();
        user.setEmail("expired@example.com");
        user.setPhone("13800138000");
        user.setPasswordHash(passwordEncoder.encode("Password123"));
        user.setStatus(User.UserStatus.APPROVED);
        user.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired yesterday
        userRepository.save(user);
        
        LoginRequest request = new LoginRequest();
        request.setEmail("expired@example.com");
        request.setPassword("Password123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("账号已到期"));
    }
    
    @Test
    void testLogin_UserNotFound() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("Password123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("邮箱或密码错误"));
    }
}
