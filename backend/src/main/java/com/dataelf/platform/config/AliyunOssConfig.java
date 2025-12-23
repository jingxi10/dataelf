package com.dataelf.platform.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
@Slf4j
public class AliyunOssConfig {
    
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String customDomain;
    private String dirPrefix;
    
    @Bean
    @ConditionalOnProperty(name = "aliyun.oss.access-key-id", matchIfMissing = false)
    public OSS ossClient() {
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            log.warn("Aliyun OSS is not configured, OSS features will be disabled");
            return null;
        }
        log.info("Initializing Aliyun OSS client, endpoint: {}, bucket: {}", endpoint, bucketName);
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
    
    /**
     * 检查OSS是否已配置
     */
    public boolean isConfigured() {
        return accessKeyId != null && !accessKeyId.isEmpty() 
            && accessKeySecret != null && !accessKeySecret.isEmpty()
            && bucketName != null && !bucketName.isEmpty();
    }
    
    /**
     * 获取文件访问URL的基础路径
     */
    public String getBaseUrl() {
        if (customDomain != null && !customDomain.isEmpty()) {
            return customDomain.startsWith("http") ? customDomain : "https://" + customDomain;
        }
        return "https://" + bucketName + "." + endpoint;
    }
}
