package com.dataelf.platform.service;

import com.dataelf.platform.entity.Content;
import com.dataelf.platform.entity.Template;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.ContentRepository;
import com.dataelf.platform.repository.TemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {
    
    private final ContentRepository contentRepository;
    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Export content as JSON-LD format
     * Validates: Requirements 9.2 - JSON-LD export must be valid Schema.org
     */
    public String exportAsJsonLd(Long contentId) {
        log.info("Exporting content {} as JSON-LD", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElse(null);
        
        if (content == null) {
            throw new ValidationException("内容不存在");
        }
        
        String jsonLd = content.getJsonLd();
        
        // Validate JSON-LD format
        validateJsonLd(jsonLd);
        
        return jsonLd;
    }
    
    /**
     * Export content as HTML format with semantic markup
     * Validates: Requirements 9.3 - HTML export must include semantic markup
     */
    public String exportAsHtml(Long contentId) {
        log.info("Exporting content {} as HTML", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElse(null);
        
        if (content == null) {
            throw new ValidationException("内容不存在");
        }
        
        String html = content.getHtmlOutput();
        
        // Validate HTML contains semantic markup
        validateHtmlSemanticMarkup(html);
        
        return html;
    }
    
    /**
     * Export content as Markdown format
     * Validates: Requirements 9.4 - Markdown export must be valid format
     */
    public String exportAsMarkdown(Long contentId) {
        log.info("Exporting content {} as Markdown", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElse(null);
        
        if (content == null) {
            throw new ValidationException("内容不存在");
        }
        
        String markdown = content.getMarkdownOutput();
        
        if (markdown == null || markdown.trim().isEmpty()) {
            throw new ValidationException("Markdown输出不存在");
        }
        
        // Validate Markdown format
        validateMarkdownFormat(markdown);
        
        return markdown;
    }
    
    /**
     * Export content as CSV format
     * Validates: Requirements 9.5 - CSV export must include all structured fields
     */
    public byte[] exportAsCsv(Long contentId) {
        log.info("Exporting content {} as CSV", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElse(null);
        
        if (content == null) {
            throw new ValidationException("内容不存在");
        }
        
        Template template = templateRepository.findById(content.getTemplateId())
            .orElse(null);
        
        if (template == null) {
            throw new ValidationException("模板不存在");
        }
        
        try {
            Map<String, Object> structuredData = objectMapper.readValue(
                content.getStructuredData(), 
                Map.class
            );
            
            Map<String, Object> schemaDefinition = objectMapper.readValue(
                template.getSchemaDefinition(), 
                Map.class
            );
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = (List<Map<String, Object>>) schemaDefinition.get("fields");
            
            if (fields == null || fields.isEmpty()) {
                throw new ValidationException("模板字段定义为空");
            }
            
            // Build CSV content
            StringBuilder csv = new StringBuilder();
            
            // Header row - field names
            List<String> fieldNames = new ArrayList<>();
            for (Map<String, Object> field : fields) {
                String fieldName = (String) field.get("name");
                fieldNames.add(fieldName);
            }
            csv.append(String.join(",", fieldNames)).append("\n");
            
            // Data row - field values
            List<String> fieldValues = new ArrayList<>();
            for (String fieldName : fieldNames) {
                Object value = structuredData.get(fieldName);
                String csvValue = value != null ? escapeCsvValue(value.toString()) : "";
                fieldValues.add(csvValue);
            }
            csv.append(String.join(",", fieldValues)).append("\n");
            
            // Add metadata rows
            csv.append("\n");
            csv.append("元数据\n");
            csv.append("标题,").append(escapeCsvValue(content.getTitle())).append("\n");
            
            if (content.getAuthorName() != null) {
                csv.append("作者,").append(escapeCsvValue(content.getAuthorName())).append("\n");
            }
            
            if (content.getContentSource() != null) {
                csv.append("来源,").append(escapeCsvValue(content.getContentSource())).append("\n");
            }
            
            if (content.getCopyrightNotice() != null) {
                csv.append("版权声明,").append(escapeCsvValue(content.getCopyrightNotice())).append("\n");
            }
            
            csv.append("原创,").append(content.getIsOriginal() ? "是" : "否").append("\n");
            
            // Validate CSV has all fields
            validateCsvFieldCompleteness(csv.toString(), fieldNames);
            
            return csv.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse content data for CSV export: {}", e.getMessage());
            throw new ValidationException("内容数据解析失败: " + e.getMessage());
        }
    }
    
    /**
     * Validate JSON-LD format and Schema.org compliance
     */
    private void validateJsonLd(String jsonLd) {
        if (jsonLd == null || jsonLd.trim().isEmpty()) {
            throw new ValidationException("JSON-LD输出为空");
        }
        
        try {
            Map<String, Object> jsonLdMap = objectMapper.readValue(jsonLd, Map.class);
            
            // Check for required Schema.org fields
            if (!jsonLdMap.containsKey("@context")) {
                throw new ValidationException("JSON-LD缺少@context字段");
            }
            
            if (!jsonLdMap.containsKey("@type")) {
                throw new ValidationException("JSON-LD缺少@type字段");
            }
            
            String context = jsonLdMap.get("@context").toString();
            if (!context.contains("schema.org")) {
                throw new ValidationException("JSON-LD的@context必须包含schema.org");
            }
            
            log.debug("JSON-LD validation passed");
            
        } catch (JsonProcessingException e) {
            throw new ValidationException("JSON-LD格式无效: " + e.getMessage());
        }
    }
    
    /**
     * Validate HTML contains semantic markup (itemscope, itemtype, itemprop)
     */
    private void validateHtmlSemanticMarkup(String html) {
        if (html == null || html.trim().isEmpty()) {
            throw new ValidationException("HTML输出为空");
        }
        
        // Check for semantic markup attributes
        boolean hasItemscope = html.contains("itemscope");
        boolean hasItemtype = html.contains("itemtype");
        boolean hasItemprop = html.contains("itemprop");
        
        if (!hasItemscope || !hasItemtype || !hasItemprop) {
            throw new ValidationException("HTML缺少语义化标记（itemscope、itemtype或itemprop）");
        }
        
        // Check for Schema.org reference
        if (!html.contains("schema.org")) {
            throw new ValidationException("HTML缺少Schema.org引用");
        }
        
        log.debug("HTML semantic markup validation passed");
    }
    
    /**
     * Validate Markdown format
     */
    private void validateMarkdownFormat(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            throw new ValidationException("Markdown输出为空");
        }
        
        // Basic Markdown validation - check for common markdown elements
        boolean hasHeading = markdown.contains("#");
        
        if (!hasHeading) {
            throw new ValidationException("Markdown格式无效：缺少标题标记");
        }
        
        log.debug("Markdown format validation passed");
    }
    
    /**
     * Validate CSV contains all required fields
     */
    private void validateCsvFieldCompleteness(String csv, List<String> expectedFields) {
        if (csv == null || csv.trim().isEmpty()) {
            throw new ValidationException("CSV输出为空");
        }
        
        // Check that all expected fields are in the CSV header
        String[] lines = csv.split("\n");
        if (lines.length < 1) {
            throw new ValidationException("CSV格式无效：没有标题行");
        }
        
        String headerLine = lines[0];
        for (String fieldName : expectedFields) {
            if (!headerLine.contains(fieldName)) {
                throw new ValidationException("CSV缺少字段: " + fieldName);
            }
        }
        
        log.debug("CSV field completeness validation passed");
    }
    
    /**
     * Escape CSV values to handle commas, quotes, and newlines
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}
