package com.dataelf.platform.service;

import com.dataelf.platform.entity.LoginAttempt;
import com.dataelf.platform.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录尝试服务
 * 实现登录失败锁定功能
 * 需求: 安全基础 - 登录失败锁定
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {
    
    private final LoginAttemptRepository loginAttemptRepository;
    
    @Value("${app.login.max-attempts:5}")
    private int maxAttempts;
    
    @Value("${app.login.lockout-duration:900000}") // 15分钟，单位毫秒
    private long lockoutDurationMs;
    
    /**
     * 记录登录尝试
     */
    @Transactional
    public void recordLoginAttempt(String email, boolean successful, String ipAddress) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setSuccessful(successful);
        attempt.setIpAddress(ipAddress);
        
        loginAttemptRepository.save(attempt);
        
        if (successful) {
            log.info("Successful login recorded for email: {}", email);
        } else {
            log.warn("Failed login attempt recorded for email: {} from IP: {}", email, ipAddress);
        }
    }
    
    /**
     * 检查账号是否被锁定
     * 如果在锁定时间窗口内失败次数超过最大尝试次数，则返回true
     */
    @Transactional(readOnly = true)
    public boolean isAccountLocked(String email) {
        LocalDateTime lockoutWindow = LocalDateTime.now().minusNanos(lockoutDurationMs * 1_000_000);
        
        List<LoginAttempt> failedAttempts = loginAttemptRepository
            .findFailedAttemptsSince(email, lockoutWindow);
        
        boolean isLocked = failedAttempts.size() >= maxAttempts;
        
        if (isLocked) {
            log.warn("Account locked for email: {} due to {} failed attempts", 
                     email, failedAttempts.size());
        }
        
        return isLocked;
    }
    
    /**
     * 获取账号剩余锁定时间（秒）
     */
    @Transactional(readOnly = true)
    public long getRemainingLockoutTime(String email) {
        LocalDateTime lockoutWindow = LocalDateTime.now().minusNanos(lockoutDurationMs * 1_000_000);
        
        List<LoginAttempt> failedAttempts = loginAttemptRepository
            .findFailedAttemptsSince(email, lockoutWindow);
        
        if (failedAttempts.size() < maxAttempts) {
            return 0;
        }
        
        // 找到最早的失败尝试
        LocalDateTime earliestFailure = failedAttempts.stream()
            .map(LoginAttempt::getCreatedAt)
            .min(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());
        
        LocalDateTime unlockTime = earliestFailure.plusNanos(lockoutDurationMs * 1_000_000);
        long remainingSeconds = java.time.Duration.between(LocalDateTime.now(), unlockTime).getSeconds();
        
        return Math.max(0, remainingSeconds);
    }
    
    /**
     * 清理过期的登录尝试记录
     * 建议通过定时任务调用
     */
    @Transactional
    public void cleanupOldAttempts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        loginAttemptRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("Cleaned up login attempts older than {}", cutoffDate);
    }
}
