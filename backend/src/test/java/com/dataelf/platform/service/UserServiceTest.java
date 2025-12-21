package com.dataelf.platform.service;

import com.dataelf.platform.dto.LoginRequest;
import com.dataelf.platform.dto.LoginResponse;
import com.dataelf.platform.dto.RegisterRequest;
import com.dataelf.platform.dto.UserDTO;
import com.dataelf.platform.entity.User;
import com.dataelf.platform.exception.AuthenticationException;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.UserRepository;
import com.dataelf.platform.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private LoginAttemptService loginAttemptService;
    
    @InjectMocks
    private UserService userService;
    
    private BCryptPasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);
        ReflectionTestUtils.setField(userService, "bcryptStrength", 12);
    }
    
    @Test
    void testRegister_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPhone("13800138000");
        request.setPassword("Password123");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });
        
        // When
        UserDTO result = userService.register(request);
        
        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("13800138000", result.getPhone());
        assertEquals(User.UserStatus.PENDING, result.getStatus());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testRegister_EmailAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPhone("13800138000");
        request.setPassword("Password123");
        
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.register(request);
        });
        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testLogin_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123"));
        user.setStatus(User.UserStatus.APPROVED);
        user.setExpiresAt(LocalDateTime.now().plusDays(30));
        
        when(loginAttemptService.isAccountLocked(anyString())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(1L, "test@example.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(1L, "test@example.com")).thenReturn("refresh-token");
        doNothing().when(loginAttemptService).recordLoginAttempt(anyString(), anyBoolean(), any());
        
        // When
        LoginResponse response = userService.login(request);
        
        // Then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
        verify(loginAttemptService, times(1)).recordLoginAttempt(eq("test@example.com"), eq(true), any());
    }
    
    @Test
    void testLogin_InvalidPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123"));
        user.setStatus(User.UserStatus.APPROVED);
        
        when(loginAttemptService.isAccountLocked(anyString())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doNothing().when(loginAttemptService).recordLoginAttempt(anyString(), anyBoolean(), any());
        
        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.login(request);
        });
        assertEquals("邮箱或密码错误", exception.getMessage());
        verify(loginAttemptService, times(1)).recordLoginAttempt(eq("test@example.com"), eq(false), any());
    }
    
    @Test
    void testLogin_PendingAccount() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123"));
        user.setStatus(User.UserStatus.PENDING);
        
        when(loginAttemptService.isAccountLocked(anyString())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(loginAttemptService).recordLoginAttempt(anyString(), anyBoolean(), any());
        
        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.login(request);
        });
        assertEquals("账号审核中", exception.getMessage());
        verify(loginAttemptService, times(1)).recordLoginAttempt(eq("test@example.com"), eq(false), any());
    }
    
    @Test
    void testLogin_ExpiredAccount() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123"));
        user.setStatus(User.UserStatus.APPROVED);
        user.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired yesterday
        
        when(loginAttemptService.isAccountLocked(anyString())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(loginAttemptService).recordLoginAttempt(anyString(), anyBoolean(), any());
        
        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.login(request);
        });
        assertEquals("账号已到期", exception.getMessage());
        verify(loginAttemptService, times(1)).recordLoginAttempt(eq("test@example.com"), eq(false), any());
    }
    
    @Test
    void testIsAccountValid_ApprovedAndNotExpired() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setStatus(User.UserStatus.APPROVED);
        user.setExpiresAt(LocalDateTime.now().plusDays(30));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // When
        boolean result = userService.isAccountValid(1L);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testIsAccountValid_PendingStatus() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setStatus(User.UserStatus.PENDING);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // When
        boolean result = userService.isAccountValid(1L);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testIsAccountValid_Expired() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setStatus(User.UserStatus.APPROVED);
        user.setExpiresAt(LocalDateTime.now().minusDays(1));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // When
        boolean result = userService.isAccountValid(1L);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testApproveUser() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setStatus(User.UserStatus.PENDING);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(notificationService).sendAccountApprovedNotification(anyString(), any(Long.class), any(Integer.class));
        
        // When
        userService.approveUser(1L, 999L, 30);
        
        // Then
        verify(userRepository, times(1)).save(argThat(u -> 
            u.getStatus() == User.UserStatus.APPROVED &&
            u.getApprovedAt() != null &&
            u.getExpiresAt() != null &&
            u.getApprovedBy() == 999L
        ));
        verify(notificationService, times(1)).sendAccountApprovedNotification("test@example.com", 1L, 30);
    }
    
    @Test
    void testExtendAccount() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setStatus(User.UserStatus.APPROVED);
        user.setExpiresAt(LocalDateTime.now().plusDays(5));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(notificationService).sendAccountExtendedNotification(anyString(), any(Long.class), any(Integer.class));
        
        // When
        userService.extendAccount(1L, 30);
        
        // Then
        verify(userRepository, times(1)).save(argThat(u -> 
            u.getExpiresAt().isAfter(LocalDateTime.now().plusDays(30))
        ));
        verify(notificationService, times(1)).sendAccountExtendedNotification("test@example.com", 1L, 30);
    }
}
