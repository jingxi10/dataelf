package com.dataelf.platform.service;

import com.dataelf.platform.dto.NotificationDTO;
import com.dataelf.platform.entity.Notification;
import com.dataelf.platform.entity.Notification.NotificationType;
import com.dataelf.platform.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    /**
     * 发送邮件（内部方法，同步）
     */
    private void doSendEmail(String to, String subject, String content) {
        try {
            if (fromEmail == null || fromEmail.isEmpty()) {
                log.warn("Mail not configured, skipping email to: {}", to);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            System.out.println("errormail:"+e.getMessage());
            log.error("Failed to send email to: {},merror:{}", to, e.getCause());
        }
    }
    
    /**
     * 创建通知（内部方法，同步）
     */
    private void doCreateNotification(Long userId, NotificationType type, String title, 
                                      String message, Long relatedContentId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setIsRead(false);
            notification.setRelatedContentId(relatedContentId);
            
            notificationRepository.save(notification);
            log.info("Notification created for user: {}, type: {}", userId, type);
        } catch (Exception e) {
            log.error("Failed to create notification for user: {}", userId, e);
        }
    }
    
    /**
     * 发送邮件（同步）
     */
    public void sendEmail(String to, String subject, String content) {
        doSendEmail(to, subject, content);
    }
    
    /**
     * 异步发送邮件
     */
    @Async
    public void sendEmailAsync(String to, String subject, String content) {
        doSendEmail(to, subject, content);
    }
    
    /**
     * 创建站内通知（同步）
     */
    @Transactional
    public NotificationDTO createNotification(Long userId, NotificationType type, String message) {
        return createNotification(userId, type, getDefaultTitle(type), message, null);
    }
    
    /**
     * 创建站内通知（带相关内容ID，同步）
     */
    @Transactional
    public NotificationDTO createNotification(Long userId, NotificationType type, String title, 
                                             String message, Long relatedContentId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setRelatedContentId(relatedContentId);
        
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created for user: {}, type: {}", userId, type);
        
        return NotificationDTO.fromEntity(saved);
    }
    
    /**
     * 异步创建站内通知
     */
    @Async
    public void createNotificationAsync(Long userId, NotificationType type, String message) {
        doCreateNotification(userId, type, getDefaultTitle(type), message, null);
    }
    
    /**
     * 异步创建站内通知（带相关内容ID）
     */
    @Async
    public void createNotificationAsync(Long userId, NotificationType type, String title, 
                                       String message, Long relatedContentId) {
        doCreateNotification(userId, type, title, message, relatedContentId);
    }

    /**
     * 获取用户所有通知
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(NotificationDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取用户未读通知
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false)
            .stream()
            .map(NotificationDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取未读通知数量
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }
    
    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
            log.info("Notification marked as read: {}", notificationId);
        });
    }
    
    /**
     * 标记用户所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = 
            notificationRepository.findByUserIdAndIsRead(userId, false);
        
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
        
        log.info("All notifications marked as read for user: {}", userId);
    }
    
    /**
     * 发送账号到期提醒
     */
    @Async
    public void sendExpiryReminder(Long userId, Integer daysLeft) {
        String message = String.format("您的账号将在 %d 天后到期，请及时联系管理员延长使用时间。", daysLeft);
        doCreateNotification(userId, NotificationType.ACCOUNT_EXPIRING, "账号即将到期", message, null);
    }
    
    /**
     * 发送账号批准通知（邮件+站内）
     */
    @Async
    public void sendAccountApprovedNotification(String email, Long userId, Integer validDays) {
        // 发送邮件
        String subject = "账号审核通过通知";
        String content = String.format(
            "您好！\n\n" +
            "您的账号已通过审核，有效期为 %d 天。\n" +
            "您现在可以登录系统开始使用。\n\n" +
            "祝您使用愉快！\n" +
            "数流精灵团队",
            validDays
        );
        doSendEmail(email, subject, content);
        
        // 创建站内通知
        String message = String.format("您的账号已通过审核，有效期为 %d 天。", validDays);
        doCreateNotification(userId, NotificationType.ACCOUNT_APPROVED, "账号审核通过", message, null);
    }
    
    /**
     * 发送内容批准通知（邮件+站内）
     */
    @Async
    public void sendContentApprovedNotification(String email, Long userId, Long contentId, String contentTitle) {
        // 发送邮件
        String subject = "内容审核通过通知";
        String content = String.format(
            "您好！\n\n" +
            "您提交的内容《%s》已通过审核。\n" +
            "您现在可以发布该内容。\n\n" +
            "祝您使用愉快！\n" +
            "数流精灵团队",
            contentTitle
        );
        doSendEmail(email, subject, content);
        
        // 创建站内通知
        String message = String.format("您的内容《%s》已通过审核，可以发布了。", contentTitle);
        doCreateNotification(userId, NotificationType.CONTENT_APPROVED, "内容审核通过", message, contentId);
    }
    
    /**
     * 发送内容拒绝通知（邮件+站内）
     */
    @Async
    public void sendContentRejectedNotification(String email, Long userId, Long contentId, 
                                               String contentTitle, String reason) {
        // 发送邮件
        String subject = "内容审核未通过通知";
        String content = String.format(
            "您好！\n\n" +
            "您提交的内容《%s》未通过审核。\n" +
            "拒绝原因：%s\n\n" +
            "请修改后重新提交。\n\n" +
            "数流精灵团队",
            contentTitle,
            reason
        );
        doSendEmail(email, subject, content);
        
        // 创建站内通知
        String message = String.format("您的内容《%s》未通过审核。原因：%s", contentTitle, reason);
        doCreateNotification(userId, NotificationType.CONTENT_REJECTED, "内容审核未通过", message, contentId);
    }
    
    /**
     * 发送账号延长通知（邮件+站内）
     */
    @Async
    public void sendAccountExtendedNotification(String email, Long userId, Integer extendedDays) {
        // 发送邮件
        String subject = "账号时长延长通知";
        String content = String.format(
            "您好！\n\n" +
            "您的账号使用时长已延长 %d 天。\n\n" +
            "祝您使用愉快！\n" +
            "数流精灵团队",
            extendedDays
        );
        doSendEmail(email, subject, content);
        
        // 创建站内通知
        String message = String.format("您的账号使用时长已延长 %d 天。", extendedDays);
        doCreateNotification(userId, NotificationType.ACCOUNT_EXTENDED, "账号时长延长", message, null);
    }
    
    /**
     * 获取通知类型的默认标题
     */
    private String getDefaultTitle(NotificationType type) {
        switch (type) {
            case ACCOUNT_APPROVED:
                return "账号审核通过";
            case CONTENT_APPROVED:
                return "内容审核通过";
            case CONTENT_REJECTED:
                return "内容审核未通过";
            case ACCOUNT_EXPIRING:
                return "账号即将到期";
            case ACCOUNT_EXTENDED:
                return "账号时长延长";
            default:
                return "系统通知";
        }
    }
}
