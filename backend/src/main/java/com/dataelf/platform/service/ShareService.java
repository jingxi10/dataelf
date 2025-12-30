package com.dataelf.platform.service;

import com.dataelf.platform.dto.ShareLinkResponse;
import com.dataelf.platform.entity.Content;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShareService {
    
    private final ContentRepository contentRepository;
    
    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;
    
    @Transactional(readOnly = true)
    public ShareLinkResponse generateShareLinks(Long contentId) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        // 允许分享所有状态的内容（包括未发布的内容）
        String contentUrl = baseUrl + "/content/" + contentId;
        String title = content.getTitle();
        String description = extractDescription(content);
        
        ShareLinkResponse response = new ShareLinkResponse();
        response.setContentUrl(contentUrl);
        response.setTitle(title);
        response.setDescription(description);
        response.setShareLinks(generateSocialMediaLinks(contentUrl, title, description));
        
        log.info("Generated share links for content: {} (status: {})", contentId, content.getStatus());
        
        return response;
    }
    
    private Map<String, String> generateSocialMediaLinks(String url, String title, String description) {
        Map<String, String> links = new HashMap<>();
        
        try {
            String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString());
            String encodedDesc = URLEncoder.encode(description, StandardCharsets.UTF_8.toString());
            
            // 微信 - 生成二维码链接
            links.put("wechat", url);
            
            // 微博
            links.put("weibo", String.format(
                "https://service.weibo.com/share/share.php?url=%s&title=%s",
                encodedUrl, encodedTitle
            ));
            
            // QQ空间
            links.put("qzone", String.format(
                "https://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey?url=%s&title=%s&summary=%s",
                encodedUrl, encodedTitle, encodedDesc
            ));
            
            // Twitter
            links.put("twitter", String.format(
                "https://twitter.com/intent/tweet?url=%s&text=%s",
                encodedUrl, encodedTitle
            ));
            
            // Facebook
            links.put("facebook", String.format(
                "https://www.facebook.com/sharer/sharer.php?u=%s",
                encodedUrl
            ));
            
            // LinkedIn
            links.put("linkedin", String.format(
                "https://www.linkedin.com/sharing/share-offsite/?url=%s",
                encodedUrl
            ));
            
            // 复制链接
            links.put("copy", url);
            
        } catch (Exception e) {
            log.error("Failed to generate share links", e);
        }
        
        return links;
    }
    
    private String extractDescription(Content content) {
        // 从结构化数据中提取描述
        String structuredData = content.getStructuredData();
        if (structuredData != null && structuredData.length() > 100) {
            return structuredData.substring(0, 100) + "...";
        }
        return content.getTitle();
    }
    
    @Transactional
    public void recordShare(Long contentId, String platform) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        // 这里可以记录分享统计
        log.info("Content {} shared to {}", contentId, platform);
        
        // 未来可以添加分享统计表
        // shareStatRepository.save(new ShareStat(contentId, platform, LocalDateTime.now()));
    }
}
