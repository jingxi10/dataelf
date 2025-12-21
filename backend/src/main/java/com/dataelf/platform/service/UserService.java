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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;
    private final LoginAttemptService loginAttemptService;
    
    @Value("${app.password.bcrypt-strength}")
    private int bcryptStrength;
    
    private BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
    
    /**
     * 用户注册
     * 需求: 1.1, 1.2
     */
    @Transactional
    public UserDTO register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());
        
        // 检查邮箱是否已存在
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new ValidationException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder().encode(request.getPassword()));
        user.setStatus(User.UserStatus.PENDING);
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}, status: PENDING", savedUser.getId());
        
        return UserDTO.fromEntity(savedUser);
    }
    
    /**
     * 用户登录
     * 需求: 1.4, 1.5
     * 安全: 登录失败锁定
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        return login(request, null);
    }
    
    /**
     * 用户登录（带IP地址）
     * 需求: 1.4, 1.5
     * 安全: 登录失败锁定
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        log.info("Processing login for email: {}", request.getEmail());
        
        // 检查账号是否被锁定
        if (loginAttemptService.isAccountLocked(request.getEmail())) {
            long remainingSeconds = loginAttemptService.getRemainingLockoutTime(request.getEmail());
            long remainingMinutes = (remainingSeconds + 59) / 60; // 向上取整
            String message = String.format("账号已被锁定，请在 %d 分钟后重试", remainingMinutes);
            log.warn("Login attempt for locked account: {}", request.getEmail());
            throw new AuthenticationException(message);
        }
        
        // 查找用户
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        // 验证密码
        if (user == null || !passwordEncoder().matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid credentials for email: {}", request.getEmail());
            loginAttemptService.recordLoginAttempt(request.getEmail(), false, ipAddress);
            throw new AuthenticationException("邮箱或密码错误");
        }
        
        // 检查账号状态
        if (!isAccountValid(user.getId())) {
            String message = getAccountStatusMessage(user);
            log.warn("Login denied for user {}: {}", user.getId(), message);
            loginAttemptService.recordLoginAttempt(request.getEmail(), false, ipAddress);
            throw new AuthenticationException(message);
        }
        
        // 记录成功登录
        loginAttemptService.recordLoginAttempt(request.getEmail(), true, ipAddress);
        
        // 生成JWT令牌（包含角色信息）
        String role = user.getRole() != null ? user.getRole().name() : "USER";
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
        
        log.info("User {} logged in successfully with role {}", user.getId(), role);
        
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUser(UserDTO.fromEntity(user));
        
        return response;
    }
    
    /**
     * 检查账号有效性
     * 需求: 1.4, 1.5, 5.3
     */
    @Cacheable(value = "user-sessions", key = "'validity_' + #userId")
    public boolean isAccountValid(Long userId) {
        log.debug("Checking account validity for user {} (cache miss)", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("用户不存在"));
        
        // 检查状态是否为APPROVED
        if (user.getStatus() != User.UserStatus.APPROVED) {
            return false;
        }
        
        // 检查是否过期
        if (user.getExpiresAt() != null && user.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取账号状态消息
     */
    private String getAccountStatusMessage(User user) {
        switch (user.getStatus()) {
            case PENDING:
                return "账号审核中";
            case REJECTED:
                return "账号已被拒绝";
            case EXPIRED:
                return "账号已到期";
            case APPROVED:
                if (user.getExpiresAt() != null && user.getExpiresAt().isBefore(LocalDateTime.now())) {
                    return "账号已到期";
                }
                return "账号状态异常";
            default:
                return "账号状态异常";
        }
    }
    
    /**
     * 批准用户账号
     * 需求: 1.3, 5.2
     */
    @Transactional
    @CacheEvict(value = "user-sessions", key = "'validity_' + #userId")
    public void approveUser(Long userId, Long adminId, Integer validDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("用户不存在"));
        
        user.setStatus(User.UserStatus.APPROVED);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovedBy(adminId);
        
        if (validDays != null && validDays > 0) {
            user.setExpiresAt(LocalDateTime.now().plusDays(validDays));
        }
        
        userRepository.save(user);
        log.info("User {} approved by admin {} with {} days validity, cache evicted", userId, adminId, validDays);
        
        // 发送账号批准通知（邮件+站内）
        notificationService.sendAccountApprovedNotification(user.getEmail(), userId, validDays);
    }
    
    /**
     * 延长账号时长
     * 需求: 5.5
     */
    @Transactional
    @CacheEvict(value = "user-sessions", key = "'validity_' + #userId")
    public void extendAccount(Long userId, Integer days) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("用户不存在"));
        
        LocalDateTime newExpiryDate;
        if (user.getExpiresAt() == null || user.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 如果已过期或没有设置过期时间，从当前时间开始计算
            newExpiryDate = LocalDateTime.now().plusDays(days);
        } else {
            // 如果未过期，在原有基础上延长
            newExpiryDate = user.getExpiresAt().plusDays(days);
        }
        
        user.setExpiresAt(newExpiryDate);
        userRepository.save(user);
        log.info("User {} account extended by {} days, new expiry: {}, cache evicted", userId, days, newExpiryDate);
        
        // 发送账号延长通知（邮件+站内）
        notificationService.sendAccountExtendedNotification(user.getEmail(), userId, days);
    }
    
    /**
     * 根据Token获取用户信息
     */
    public UserDTO getUserFromToken(String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("用户不存在"));
        
        // 检查用户状态
        if (user.getStatus() != User.UserStatus.APPROVED) {
            throw new AuthenticationException("账号状态异常");
        }
        
        // 检查是否过期
        if (user.getExpiresAt() != null && user.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("账号已过期");
        }
        
        return UserDTO.fromEntity(user);
    }
}
