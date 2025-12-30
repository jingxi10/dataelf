package com.dataelf.platform.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.dataelf.platform.config.AliyunOssConfig;
import com.dataelf.platform.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {
    
    private final OSS ossClient;
    private final AliyunOssConfig ossConfig;
    
    @Autowired
    public FileUploadService(@Autowired(required = false) OSS ossClient, AliyunOssConfig ossConfig) {
        this.ossClient = ossClient;
        this.ossConfig = ossConfig;
    }
    
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB
    
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );
    
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
        "video/mp4", "video/webm", "video/ogg", "video/quicktime",
        "video/x-msvideo", "video/avi", "video/x-matroska", "video/mkv"
    );
    
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
        "application/pdf", 
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    
    /**
     * 上传图片到阿里云OSS
     */
    public String uploadImage(MultipartFile file, Long userId) throws IOException {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "图片");
        return uploadToOss(file, "images", userId);
    }
    
    /**
     * 上传视频到阿里云OSS
     */
    public String uploadVideo(MultipartFile file, Long userId) throws IOException {
        validateFile(file, ALLOWED_VIDEO_TYPES, MAX_VIDEO_SIZE, "视频");
        return uploadToOss(file, "videos", userId);
    }
    
    /**
     * 上传文档到阿里云OSS
     */
    public String uploadDocument(MultipartFile file, Long userId) throws IOException {
        validateFile(file, ALLOWED_DOCUMENT_TYPES, MAX_IMAGE_SIZE, "文档");
        return uploadToOss(file, "documents", userId);
    }
    
    /**
     * 通用文件上传（自动识别类型）
     */
    public String uploadFile(MultipartFile file, Long userId) throws IOException {
        String contentType = file.getContentType();
        
        if (contentType != null) {
            if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
                return uploadImage(file, userId);
            } else if (ALLOWED_VIDEO_TYPES.contains(contentType)) {
                return uploadVideo(file, userId);
            } else if (ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
                return uploadDocument(file, userId);
            }
        }
        
        throw new ValidationException("不支持的文件类型: " + contentType);
    }
    
    /**
     * 上传文件到阿里云OSS
     */
    private String uploadToOss(MultipartFile file, String subDir, Long userId) throws IOException {
        // 检查OSS是否配置
        if (ossClient == null || !ossConfig.isConfigured()) {
            throw new ValidationException("OSS未配置，无法上传文件。请先配置阿里云OSS。");
        }
        
        // 生成文件路径：dirPrefix/subDir/yyyy/MM/dd/uuid.ext
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + extension;
        
        String objectKey = ossConfig.getDirPrefix() + subDir + "/" + dateDir + "/" + filename;
        
        // 设置文件元数据
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        // 设置缓存控制
        metadata.setCacheControl("max-age=31536000"); // 1年缓存
        
        // 上传到OSS
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = new PutObjectRequest(
                ossConfig.getBucketName(), 
                objectKey, 
                inputStream, 
                metadata
            );
            
            ossClient.putObject(putRequest);
            
            // 生成访问URL
            String baseUrl = ossConfig.getBaseUrl();
            // 确保baseUrl不以/结尾，objectKey不以/开头
            String fileUrl;
            if (baseUrl.endsWith("/")) {
                fileUrl = baseUrl + objectKey;
            } else {
                fileUrl = baseUrl + "/" + objectKey;
            }
            log.info("File uploaded to OSS by user {}: {} (size: {} bytes)", userId, fileUrl, file.getSize());
            
            return fileUrl;
        } catch (Exception e) {
            log.error("Failed to upload file to OSS", e);
            throw new IOException("文件上传到OSS失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从阿里云OSS删除文件
     */
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        
        // 检查OSS是否配置
        if (ossClient == null || !ossConfig.isConfigured()) {
            log.warn("OSS not configured, cannot delete file");
            return false;
        }
        
        try {
            // 从URL中提取objectKey
            String baseUrl = ossConfig.getBaseUrl();
            if (!fileUrl.startsWith(baseUrl)) {
                log.warn("File URL does not match OSS base URL: {}", fileUrl);
                return false;
            }
            
            String objectKey = fileUrl.substring(baseUrl.length() + 1); // +1 for the "/"
            
            // 检查文件是否存在
            boolean exists = ossClient.doesObjectExist(ossConfig.getBucketName(), objectKey);
            if (!exists) {
                log.warn("File does not exist in OSS: {}", objectKey);
                return false;
            }
            
            // 删除文件
            ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
            log.info("File deleted from OSS: {}", objectKey);
            
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from OSS: {}", fileUrl, e);
            return false;
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, List<String> allowedTypes, long maxSize, String typeName) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("文件不能为空");
        }
        
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        // 如果contentType为空，尝试从文件名推断
        if (contentType == null && filename != null) {
            String extension = getFileExtension(filename).toLowerCase();
            if (extension.equals(".mp4")) {
                contentType = "video/mp4";
            } else if (extension.equals(".webm")) {
                contentType = "video/webm";
            } else if (extension.equals(".ogg") || extension.equals(".ogv")) {
                contentType = "video/ogg";
            } else if (extension.equals(".mov")) {
                contentType = "video/quicktime";
            } else if (extension.equals(".avi")) {
                contentType = "video/x-msvideo";
            } else if (extension.equals(".mkv")) {
                contentType = "video/x-matroska";
            }
        }
        
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new ValidationException("不支持的" + typeName + "格式: " + contentType + 
                (filename != null ? " (文件: " + filename + ")" : ""));
        }
        
        if (file.getSize() > maxSize) {
            throw new ValidationException(typeName + "大小不能超过 " + (maxSize / 1024 / 1024) + "MB，当前文件大小: " + 
                String.format("%.2f", file.getSize() / 1024.0 / 1024.0) + "MB");
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
