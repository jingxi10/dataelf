package com.dataelf.platform.service;

import com.dataelf.platform.entity.Content;
import com.dataelf.platform.entity.Template;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTML生成服务 - 生成AI友好的HTML页面
 * 
 * 验证需求: 6.1, 6.2, 6.4, 6.5
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HtmlGenerationService {
    
    private final ObjectMapper objectMapper;
    
    /**
     * 生成AI友好的内容页面HTML
     * - 优先输出JSON-LD
     * - 使用语义化HTML标记
     * - 用户交互元素延迟加载
     * 
     * 验证需求: 6.1, 6.2, 6.5
     */
    public String generateContentHtml(
        Map<String, Object> structuredData,
        Template template,
        String title,
        String copyrightNotice,
        String contentSource,
        String authorName,
        Boolean isOriginal,
        List<String> fieldOrder,
        String jsonLd,
        Long contentId
    ) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("  <title>").append(escapeHtml(title)).append("</title>\n");
        
        // Priority: JSON-LD in head (Requirement 6.1)
        html.append("  <script type=\"application/ld+json\">\n");
        html.append(jsonLd);
        html.append("\n  </script>\n");
        
        // Link to pure JSON-LD API
        html.append("  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/")
            .append(contentId).append("\">\n");
        
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Main content with semantic markup (Requirement 6.2)
        html.append("  <main itemscope itemtype=\"https://schema.org/").append(template.getSchemaOrgType()).append("\">\n");
        html.append("    <h1 itemprop=\"headline\">").append(escapeHtml(title)).append("</h1>\n");
        
        // Add structured data fields with semantic markup
        List<String> keys = fieldOrder != null ? fieldOrder : new ArrayList<>(structuredData.keySet());
        for (String key : keys) {
            if (structuredData.containsKey(key)) {
                Object value = structuredData.get(key);
                html.append("    <div class=\"field\">\n");
                html.append("      <strong>").append(escapeHtml(key)).append(":</strong>\n");
                html.append("      <span itemprop=\"").append(escapeHtml(key)).append("\">")
                    .append(escapeHtml(String.valueOf(value))).append("</span>\n");
                html.append("    </div>\n");
            }
        }
        
        // Add copyright section with semantic markup
        if (authorName != null || copyrightNotice != null || contentSource != null) {
            html.append("    <footer class=\"copyright-section\">\n");
            
            if (authorName != null && !authorName.isEmpty()) {
                html.append("      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n");
                html.append("        <span>作者：</span><span itemprop=\"name\">").append(escapeHtml(authorName)).append("</span>\n");
                html.append("      </div>\n");
            }
            
            if (copyrightNotice != null && !copyrightNotice.isEmpty()) {
                html.append("      <div itemprop=\"copyrightNotice\">版权声明：").append(escapeHtml(copyrightNotice)).append("</div>\n");
            }
            
            if (contentSource != null && !contentSource.isEmpty()) {
                html.append("      <div>来源：<span itemprop=\"sourceOrganization\">").append(escapeHtml(contentSource)).append("</span></div>\n");
            }
            
            html.append("    </footer>\n");
        }
        
        html.append("  </main>\n");
        
        // User interaction elements - delayed loading (Requirement 6.5)
        html.append("\n  <!-- User interactions loaded via JavaScript -->\n");
        html.append("  <div id=\"user-interactions\" data-content-id=\"").append(contentId).append("\"></div>\n");
        html.append("\n  <script defer src=\"/static/js/interactions.js\"></script>\n");
        
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * 生成用户页面HTML (带robots noindex标签)
     * 
     * 验证需求: 6.4
     */
    public String generateUserPageHtml(String title, String content, boolean blockAI) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("  <title>").append(escapeHtml(title)).append("</title>\n");
        
        // Block AI crawlers for user pages (Requirement 6.4)
        if (blockAI) {
            html.append("  <meta name=\"robots\" content=\"noindex, nofollow\">\n");
        }
        
        html.append("</head>\n");
        html.append("<body>\n");
        html.append(content);
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * 检查路径是否为用户页面
     * 
     * 验证需求: 6.4
     */
    public boolean isUserPage(String path) {
        return path != null && (
            path.startsWith("/user/") ||
            path.startsWith("/account/") ||
            path.startsWith("/profile/") ||
            path.startsWith("/settings/")
        );
    }
    
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
