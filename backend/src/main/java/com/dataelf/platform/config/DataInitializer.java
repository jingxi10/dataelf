package com.dataelf.platform.config;

import com.dataelf.platform.entity.User;
import com.dataelf.platform.repository.UserRepository;
import com.dataelf.platform.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 * 在应用启动时初始化默认配置和管理员账号
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final SystemConfigService systemConfigService;
    private final UserRepository userRepository;
    
    @Override
    public void run(String... args) {
        log.info("Initializing default system configurations...");
        
        try {
            systemConfigService.initializeDefaultConfigs();
            log.info("Default system configurations initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize default configurations", e);
        }
        
        // 初始化默认管理员账号
        initializeAdminUser();
    }
    
    private void initializeAdminUser() {
        String adminEmail = "admin@dataelf.com";
        
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("Creating default admin user...");
            
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPhone("13800000000");
            admin.setPasswordHash(new BCryptPasswordEncoder().encode("admin123"));
            admin.setRole(User.UserRole.ADMIN);
            admin.setAdminType(User.AdminType.MAIN_ADMIN); // 设置为主管理员
            admin.setStatus(User.UserStatus.APPROVED);
            admin.setApprovedAt(LocalDateTime.now());
            admin.setExpiresAt(LocalDateTime.of(2037, 12, 31, 23, 59, 59)); // 永久有效
            
            userRepository.save(admin);
            
            log.info("==============================================");
            log.info("Default admin user created:");
            log.info("  Email: {}", adminEmail);
            log.info("  Password: admin123");
            log.info("  Please change the password after first login!");
            log.info("==============================================");
        } else {
            log.info("Admin user already exists, skipping creation");
        }
    }
}
