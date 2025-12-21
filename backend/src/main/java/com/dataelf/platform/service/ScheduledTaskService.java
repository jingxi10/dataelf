package com.dataelf.platform.service;

import com.dataelf.platform.entity.User;
import com.dataelf.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {
    
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final LoginAttemptService loginAttemptService;
    
    /**
     * 每天检查即将到期的账号并发送提醒
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkExpiringAccounts() {
        log.info("Starting expiring accounts check...");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);
        
        // 查找所有已批准且在7天内到期的账号
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            if (user.getStatus() == User.UserStatus.APPROVED && 
                user.getExpiresAt() != null &&
                user.getExpiresAt().isAfter(now) &&
                user.getExpiresAt().isBefore(sevenDaysLater)) {
                
                long daysLeft = ChronoUnit.DAYS.between(now, user.getExpiresAt());
                
                // 只在7天、3天、1天时发送提醒
                if (daysLeft == 7 || daysLeft == 3 || daysLeft == 1) {
                    notificationService.sendExpiryReminder(user.getId(), (int) daysLeft);
                    
                    // 同时发送邮件提醒
                    String subject = "账号即将到期提醒";
                    String content = String.format(
                        "您好！\n\n" +
                        "您的账号将在 %d 天后到期，请及时联系管理员延长使用时间。\n\n" +
                        "数流精灵团队",
                        daysLeft
                    );
                    notificationService.sendEmailAsync(user.getEmail(), subject, content);
                    
                    log.info("Expiry reminder sent to user: {}, days left: {}", user.getId(), daysLeft);
                }
            }
        }
        
        log.info("Expiring accounts check completed");
    }
    
    /**
     * 清理过期的登录尝试记录
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldLoginAttempts() {
        log.info("Starting login attempts cleanup...");
        loginAttemptService.cleanupOldAttempts();
        log.info("Login attempts cleanup completed");
    }
}
