package com.dataelf.platform.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.dataelf.platform.config.AliyunOssConfig;
import com.dataelf.platform.entity.SystemConfig;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.SystemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class SystemConfigService {
    
    private final SystemConfigRepository systemConfigRepository;
    private final OSS ossClient;
    private final AliyunOssConfig ossConfig;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    
    @Autowired
    public SystemConfigService(SystemConfigRepository systemConfigRepository, 
                               @Autowired(required = false) OSS ossClient,
                               AliyunOssConfig ossConfig) {
        this.systemConfigRepository = systemConfigRepository;
        this.ossClient = ossClient;
        this.ossConfig = ossConfig;
    }
    
    @Transactional(readOnly = true)
    public String getConfigValue(String key) {
        return systemConfigRepository.findByConfigKey(key)
            .map(SystemConfig::getConfigValue)
            .orElse(null);
    }
    
    @Transactional(readOnly = true)
    public String getConfigValue(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
            .map(SystemConfig::getConfigValue)
            .orElse(defaultValue);
    }
    
    @Transactional
    public void setConfigValue(String key, String value, String description) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
            .orElse(new SystemConfig());
        
        config.setConfigKey(key);
        config.setConfigValue(value);
        if (description != null) {
            config.setDescription(description);
        }
        
        systemConfigRepository.save(config);
        log.info("System config updated: key={}", key);
    }
    
    @Transactional(readOnly = true)
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = systemConfigRepository.findAll();
        Map<String, String> result = new HashMap<>();
        
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        
        return result;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getPublicConfigs() {
        Map<String, Object> result = new HashMap<>();
        
        // 只返回公开的配置
        result.put("siteName", getConfigValue("site.name", "数流精灵"));
        result.put("siteDescription", getConfigValue("site.description", "去伪存真、建立AI秩序"));
        result.put("logoUrl", getConfigValue("site.logo.url", ""));
        result.put("heroTitle", getConfigValue("site.hero.title", "去伪存真、建立AI秩序"));
        result.put("heroSubtitle", getConfigValue("site.hero.subtitle", "专为AI优化的结构化数据平台"));
        result.put("heroBgColor", getConfigValue("site.hero.bgColor", ""));
        result.put("heroTextColor", getConfigValue("site.hero.textColor", ""));
        result.put("footerLinks", getConfigValue("site.footer.links", getDefaultFooterLinks()));
        result.put("footerBottomLinks", getConfigValue("site.footer.bottomLinks", getDefaultFooterBottomLinks()));
        result.put("footerIcp", getConfigValue("site.footer.icp", ""));
        result.put("footerCopyright", getConfigValue("site.footer.copyright", ""));
        result.put("socialLinks", getConfigValue("site.social.links", getDefaultSocialLinks()));
        
        return result;
    }
    
    private String getDefaultFooterLinks() {
        return "[{\"group\":\"产品功能\",\"links\":[{\"name\":\"结构化模板\",\"url\":\"#\"},{\"name\":\"内容编辑器\",\"url\":\"#\"},{\"name\":\"数据探索\",\"url\":\"#\"},{\"name\":\"API文档\",\"url\":\"#\"}]},{\"group\":\"资源中心\",\"links\":[{\"name\":\"帮助文档\",\"url\":\"#\"},{\"name\":\"教程指南\",\"url\":\"#\"},{\"name\":\"博客文章\",\"url\":\"#\"},{\"name\":\"常见问题\",\"url\":\"#\"}]},{\"group\":\"关于我们\",\"links\":[{\"name\":\"公司介绍\",\"url\":\"#\"},{\"name\":\"联系我们\",\"url\":\"#\"},{\"name\":\"隐私政策\",\"url\":\"#\"},{\"name\":\"服务条款\",\"url\":\"#\"}]}]";
    }
    
    private String getDefaultFooterBottomLinks() {
        return "[{\"name\":\"网站地图\",\"url\":\"#\"},{\"name\":\"AI数据接口\",\"url\":\"#\"},{\"name\":\"机器人协议\",\"url\":\"#\"}]";
    }
    
    private String getDefaultSocialLinks() {
        return "{\"share\":\"#\",\"comment\":\"#\",\"link\":\"#\"}";
    }
    
    @Transactional
    public String uploadLogo(MultipartFile file) throws IOException {
        // 检查OSS是否配置
        if (ossClient == null || !ossConfig.isConfigured()) {
            throw new ValidationException("OSS未配置，无法上传文件。请先配置阿里云OSS。");
        }
        
        // 验证文件
        validateImageFile(file);
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".png";
        String filename = "logo_" + UUID.randomUUID().toString() + extension;
        
        // 上传到阿里云OSS
        String objectKey = ossConfig.getDirPrefix() + "system/" + filename;
        
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        metadata.setCacheControl("max-age=31536000");
        
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = new PutObjectRequest(
                ossConfig.getBucketName(),
                objectKey,
                inputStream,
                metadata
            );
            
            ossClient.putObject(putRequest);
            
            // 生成访问URL
            String logoUrl = ossConfig.getBaseUrl() + "/" + objectKey;
            
            // 保存到配置
            setConfigValue("site.logo.url", logoUrl, "网站Logo URL");
            
            log.info("Logo uploaded to OSS successfully: {}", logoUrl);
            
            return logoUrl;
        } catch (Exception e) {
            log.error("Failed to upload logo to OSS", e);
            throw new IOException("Logo上传到OSS失败: " + e.getMessage(), e);
        }
    }
    
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("文件大小不能超过5MB");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new ValidationException("只支持JPG、PNG、GIF、WebP格式的图片");
        }
    }
    
    @Transactional
    public void initializeDefaultConfigs() {
        // 初始化默认配置
        setConfigValueIfNotExists("site.name", "数流精灵", "网站名称");
        setConfigValueIfNotExists("site.description", "去伪存真、建立AI秩序", "网站描述");
        setConfigValueIfNotExists("site.hero.title", "去伪存真、建立AI秩序", "首页标题");
        setConfigValueIfNotExists("site.hero.subtitle", "专为AI优化的结构化数据平台", "首页副标题");
        setConfigValueIfNotExists("site.logo.url", "", "网站Logo URL");
    }
    
    private void setConfigValueIfNotExists(String key, String value, String description) {
        if (!systemConfigRepository.findByConfigKey(key).isPresent()) {
            setConfigValue(key, value, description);
        }
    }
}
