package com.dataelf.platform.service;

import com.dataelf.platform.dto.*;
import com.dataelf.platform.entity.Template;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.TemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {
    
    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper;
    
    @PostConstruct
    public void initializeSystemTemplates() {
        if (templateRepository.findByIsSystem(true).isEmpty()) {
            log.info("初始化系统预设模板...");
            createSystemTemplate("技术文章", "TechArticle", "技术类文章模板", 
                createTechArticleSchema(), "Article");
            createSystemTemplate("案例研究", "CaseStudy", "案例研究模板", 
                createCaseStudySchema(), "Article");
            createSystemTemplate("数据报告", "DataReport", "数据报告模板", 
                createDataReportSchema(), "Report");
            createSystemTemplate("产品评测", "ProductReview", "产品评测模板", 
                createProductReviewSchema(), "Review");
            createSystemTemplate("调查报告", "SurveyReport", "调查报告模板", 
                createSurveyReportSchema(), "Report");
            createSystemTemplate("行业分析", "IndustryAnalysis", "行业分析模板", 
                createIndustryAnalysisSchema(), "AnalysisNewsArticle");
            log.info("系统预设模板初始化完成");
        }
    }
    
    private void createSystemTemplate(String name, String type, String description, 
                                      String schemaDefinition, String schemaOrgType) {
        Template template = new Template();
        template.setName(name);
        template.setType(type);
        template.setDescription(description);
        template.setSchemaDefinition(schemaDefinition);
        template.setSchemaOrgType(schemaOrgType);
        template.setIsSystem(true);
        templateRepository.save(template);
    }
    
    private String createTechArticleSchema() {
        return """
            {
              "@context": "https://schema.org",
              "@type": "Article",
              "fields": [
                {"name": "headline", "type": "string", "required": true, "label": "标题"},
                {"name": "author", "type": "Person", "required": true, "label": "作者"},
                {"name": "datePublished", "type": "date", "required": true, "label": "发布日期"},
                {"name": "articleBody", "type": "text", "required": true, "label": "正文"},
                {"name": "keywords", "type": "array", "required": false, "label": "关键词"},
                {"name": "image", "type": "url", "required": false, "label": "配图"}
              ]
            }
            """;
    }
    
    private String createCaseStudySchema() {
        return """
            {
              "@context": "https://schema.org",
              "@type": "Article",
              "fields": [
                {"name": "headline", "type": "string", "required": true, "label": "案例标题"},
                {"name": "author", "type": "Person", "required": true, "label": "作者"},
                {"name": "datePublished", "type": "date", "required": true, "label": "发布日期"},
                {"name": "articleBody", "type": "text", "required": true, "label": "案例内容"},
                {"name": "about", "type": "Thing", "required": true, "label": "案例主题"},
                {"name": "keywords", "type": "array", "required": false, "label": "关键词"}
              ]
            }
            """;
    }
    
    private String createDataReportSchema() {
        return """
            {
              "@context": "https://schema.org",
              "@type": "Report",
              "fields": [
                {"name": "name", "type": "string", "required": true, "label": "报告名称"},
                {"name": "author", "type": "Person", "required": true, "label": "作者"},
                {"name": "datePublished", "type": "date", "required": true, "label": "发布日期"},
                {"name": "text", "type": "text", "required": true, "label": "报告内容"},
                {"name": "about", "type": "Thing", "required": true, "label": "报告主题"},
                {"name": "keywords", "type": "array", "required": false, "label": "关键词"}
              ]
            }
            """;
    }
    
    private String createProductReviewSchema() {
        return """
            {
              "@context": "https://schema.org",
              "@type": "Review",
              "fields": [
                {"name": "name", "type": "string", "required": true, "label": "评测标题"},
                {"name": "author", "type": "Person", "required": true, "label": "作者"},
                {"name": "datePublished", "type": "date", "required": true, "label": "发布日期"},
                {"name": "reviewBody", "type": "text", "required": true, "label": "评测内容"},
                {"name": "itemReviewed", "type": "Product", "required": true, "label": "评测产品"},
                {"name": "reviewRating", "type": "Rating", "required": false, "label": "评分"}
              ]
            }
            """;
    }
    
    private String createSurveyReportSchema() {
        return """
            {
              "@context": "https://schema.org",
              "@type": "Report",
              "fields": [
                {"name": "name", "type": "string", "required": true, "label": "调查报告名称"},
                {"name": "author", "type": "Person", "required": true, "label": "作者"},
                {"name": "datePublished", "type": "date", "required": true, "label": "发布日期"},
                {"name": "text", "type": "text", "required": true, "label": "调查内容"},
                {"name": "about", "type": "Thing", "required": true, "label": "调查主题"},
                {"name": "keywords", "type": "array", "required": false, "label": "关键词"}
              ]
            }
            """;
    }
    
    private String createIndustryAnalysisSchema() {
        return """
            {
              "@context": "https://schema.org",
              "@type": "AnalysisNewsArticle",
              "fields": [
                {"name": "headline", "type": "string", "required": true, "label": "分析标题"},
                {"name": "author", "type": "Person", "required": true, "label": "作者"},
                {"name": "datePublished", "type": "date", "required": true, "label": "发布日期"},
                {"name": "articleBody", "type": "text", "required": true, "label": "分析内容"},
                {"name": "about", "type": "Thing", "required": true, "label": "分析主题"},
                {"name": "keywords", "type": "array", "required": false, "label": "关键词"}
              ]
            }
            """;
    }
    
    @Transactional
    @CacheEvict(value = "template-definitions", allEntries = true)
    public TemplateDTO createTemplate(TemplateCreateRequest request, Long userId) {
        // 验证Schema.org标记
        ValidationResult validationResult = validateSchema(request.getSchemaDefinition(), request.getSchemaOrgType());
        if (!validationResult.isValid()) {
            throw new ValidationException("模板验证失败: " + String.join(", ", validationResult.getErrors()));
        }
        
        Template template = new Template();
        template.setName(request.getName());
        template.setType(request.getType());
        template.setDescription(request.getDescription());
        template.setSchemaDefinition(request.getSchemaDefinition());
        template.setSchemaOrgType(request.getSchemaOrgType());
        template.setIsSystem(false);
        template.setCreatedBy(userId);
        
        Template saved = templateRepository.save(template);
        log.info("Template {} created, cache evicted", saved.getId());
        return convertToDTO(saved);
    }
    
//    @Cacheable(value = "template-definitions", key = "'all'")
    public List<TemplateDTO> getAllTemplates() {
        log.debug("Fetching all templates from database (cache miss)");
        return templateRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    

    public TemplateDTO getTemplate(Long templateId) {
        log.debug("Fetching template {} from database (cache miss)", templateId);
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new ValidationException("模板不存在"));
        return convertToDTO(template);
    }
    
    @Transactional

    public TemplateDTO updateTemplate(Long templateId, TemplateUpdateRequest request) {
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new ValidationException("模板不存在"));
        
        if (template.getIsSystem()) {
            throw new ValidationException("系统模板不能修改");
        }
        
        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getType() != null) {
            template.setType(request.getType());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getSchemaDefinition() != null && request.getSchemaOrgType() != null) {
            ValidationResult validationResult = validateSchema(request.getSchemaDefinition(), request.getSchemaOrgType());
            if (!validationResult.isValid()) {
                throw new ValidationException("模板验证失败: " + String.join(", ", validationResult.getErrors()));
            }
            template.setSchemaDefinition(request.getSchemaDefinition());
            template.setSchemaOrgType(request.getSchemaOrgType());
        }
        
        Template updated = templateRepository.save(template);
        log.info("Template {} updated, cache evicted", templateId);
        return convertToDTO(updated);
    }
    
    @Transactional
    @CacheEvict(value = "template-definitions", allEntries = true)
    public void deleteTemplate(Long templateId) {
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new ValidationException("模板不存在"));
        
        if (template.getIsSystem()) {
            throw new ValidationException("系统模板不能删除");
        }
        
        // 检查是否有内容使用该模板
        Long contentCount = templateRepository.countContentsByTemplateId(templateId);
        if (contentCount > 0) {
            throw new ValidationException("该模板正在被 " + contentCount + " 个内容使用，无法删除");
        }
        
        templateRepository.delete(template);
        log.info("Template {} deleted, cache evicted", templateId);
    }
    
    public String exportTemplate(Long templateId) {
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new ValidationException("模板不存在"));
        
        try {
            TemplateDTO dto = convertToDTO(template);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new ValidationException("模板导出失败: " + e.getMessage());
        }
    }
    
    @Transactional
    @CacheEvict(value = "template-definitions", allEntries = true)
    public TemplateDTO importTemplate(String jsonContent, Long userId) {
        try {
            TemplateDTO dto = objectMapper.readValue(jsonContent, TemplateDTO.class);
            
            // 验证Schema.org标记
            ValidationResult validationResult = validateSchema(dto.getSchemaDefinition(), dto.getSchemaOrgType());
            if (!validationResult.isValid()) {
                throw new ValidationException("导入的模板验证失败: " + String.join(", ", validationResult.getErrors()));
            }
            
            Template template = new Template();
            template.setName(dto.getName());
            template.setType(dto.getType());
            template.setDescription(dto.getDescription());
            template.setSchemaDefinition(dto.getSchemaDefinition());
            template.setSchemaOrgType(dto.getSchemaOrgType());
            template.setIsSystem(false);
            template.setCreatedBy(userId);
            
            Template saved = templateRepository.save(template);
            log.info("Template {} imported, cache evicted", saved.getId());
            return convertToDTO(saved);
        } catch (JsonProcessingException e) {
            throw new ValidationException("模板导入失败: JSON格式无效");
        }
    }
    
    public ValidationResult validateSchema(String schemaDefinition, String schemaOrgType) {
        ValidationResult result = new ValidationResult(true);
        
        try {
            JsonNode schemaNode = objectMapper.readTree(schemaDefinition);
            
            // 检查必需的@context字段
            if (!schemaNode.has("@context")) {
                result.addError("缺少必需的@context字段");
            } else {
                String context = schemaNode.get("@context").asText();
                if (!context.contains("schema.org")) {
                    result.addError("@context必须包含schema.org");
                }
            }
            
            // 检查必需的@type字段
            if (!schemaNode.has("@type")) {
                result.addError("缺少必需的@type字段");
            } else {
                String type = schemaNode.get("@type").asText();
                if (!type.equals(schemaOrgType)) {
                    result.addError("@type字段值与schemaOrgType不匹配");
                }
            }
            
            // 检查fields数组
            if (!schemaNode.has("fields")) {
                result.addError("缺少fields字段定义");
            } else if (!schemaNode.get("fields").isArray()) {
                result.addError("fields必须是数组类型");
            }
            
        } catch (JsonProcessingException e) {
            result.addError("Schema定义不是有效的JSON格式");
        }
        
        return result;
    }
    
    public ValidationResult validateSchema(TemplateDTO template) {
        return validateSchema(template.getSchemaDefinition(), template.getSchemaOrgType());
    }
    
    private TemplateDTO convertToDTO(Template template) {
        TemplateDTO dto = new TemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setType(template.getType());
        dto.setDescription(template.getDescription());
        dto.setSchemaDefinition(template.getSchemaDefinition());
        dto.setSchemaOrgType(template.getSchemaOrgType());
        dto.setIsSystem(template.getIsSystem());
        dto.setCreatedBy(template.getCreatedBy());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        return dto;
    }
}
