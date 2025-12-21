package com.dataelf.platform.service;

import com.dataelf.platform.dto.*;
import com.dataelf.platform.entity.*;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {
    
    private final ContentRepository contentRepository;
    private final TemplateRepository templateRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final HtmlGenerationService htmlGenerationService;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public ContentDTO createContent(Long userId, ContentCreateRequest request) {
        log.info("Creating content for user: {}, template: {}", userId, request.getTemplateId());
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ValidationException("用户不存在");
        }
        
        // Validate template exists
        Template template = templateRepository.findById(request.getTemplateId())
            .orElseThrow(() -> new ValidationException("模板不存在"));
        
        // Validate structured data
        ValidationResult validation = validateStructuredData(request.getStructuredData(), template);
        if (!validation.isValid()) {
            throw new ValidationException("结构化数据验证失败: " + String.join(", ", validation.getErrors()));
        }
        
        Content content = new Content();
        content.setUserId(userId);
        content.setTemplateId(request.getTemplateId());
        content.setTitle(request.getTitle());
        
        // Store structured data as JSON
        try {
            content.setStructuredData(objectMapper.writeValueAsString(request.getStructuredData()));
        } catch (JsonProcessingException e) {
            throw new ValidationException("结构化数据序列化失败: " + e.getMessage());
        }
        
        // Generate multi-format outputs
        MultiFormatOutput output = generateMultiFormatOutput(
            request.getStructuredData(), 
            template, 
            request.getTitle(),
            request.getCopyrightNotice(),
            request.getContentSource(),
            request.getAuthorName(),
            request.getIsOriginal(),
            request.getFieldOrder()
        );
        
        content.setJsonLd(output.getJsonLd());
        content.setHtmlOutput(output.getHtml());
        content.setMarkdownOutput(output.getMarkdown());
        
        // Set copyright information
        content.setCopyrightNotice(request.getCopyrightNotice());
        content.setContentSource(request.getContentSource());
        content.setAuthorName(request.getAuthorName());
        content.setIsOriginal(request.getIsOriginal() != null ? request.getIsOriginal() : true);
        
        // Calculate integrity score
        content.setIntegrityScore(calculateIntegrityScore(request.getStructuredData(), template));
        
        content.setStatus(Content.ContentStatus.DRAFT);
        
        // Handle categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            content.setCategories(categories);
        }
        
        // Handle tags
        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            Set<Tag> tags = getOrCreateTags(request.getTagNames());
            content.setTags(tags);
        }
        
        content = contentRepository.save(content);
        log.info("Content created with ID: {}", content.getId());
        
        // Update HTML with contentId for interaction loading
        String updatedHtml = content.getHtmlOutput().replace(
            "data-content-id=\"null\"",
            "data-content-id=\"" + content.getId() + "\""
        );
        content.setHtmlOutput(updatedHtml);
        content = contentRepository.save(content);
        
        return convertToDTO(content);
    }
    
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "homepage-contents", allEntries = true, condition = "#result != null and #result.status.name() == 'PUBLISHED'"),
        @CacheEvict(value = "ai-content-jsonld", key = "#contentId"),
        @CacheEvict(value = "ai-search-results", allEntries = true, condition = "#result != null and #result.status.name() == 'PUBLISHED'"),
        @CacheEvict(value = "ai-sitemap", allEntries = true, condition = "#result != null and #result.status.name() == 'PUBLISHED'")
    })
    public ContentDTO updateContent(Long contentId, ContentUpdateRequest request) {
        log.info("Updating content: {}", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        Template template = templateRepository.findById(content.getTemplateId())
            .orElseThrow(() -> new ValidationException("模板不存在"));
        
        boolean needsRegeneration = false;
        
        if (request.getTitle() != null) {
            content.setTitle(request.getTitle());
            needsRegeneration = true;
        }
        
        if (request.getStructuredData() != null) {
            ValidationResult validation = validateStructuredData(request.getStructuredData(), template);
            if (!validation.isValid()) {
                throw new ValidationException("结构化数据验证失败: " + String.join(", ", validation.getErrors()));
            }
            
            try {
                content.setStructuredData(objectMapper.writeValueAsString(request.getStructuredData()));
            } catch (JsonProcessingException e) {
                throw new ValidationException("结构化数据序列化失败: " + e.getMessage());
            }
            
            content.setIntegrityScore(calculateIntegrityScore(request.getStructuredData(), template));
            needsRegeneration = true;
        }
        
        if (request.getCopyrightNotice() != null) {
            content.setCopyrightNotice(request.getCopyrightNotice());
            needsRegeneration = true;
        }
        
        if (request.getContentSource() != null) {
            content.setContentSource(request.getContentSource());
            needsRegeneration = true;
        }
        
        if (request.getAuthorName() != null) {
            content.setAuthorName(request.getAuthorName());
            needsRegeneration = true;
        }
        
        if (request.getIsOriginal() != null) {
            content.setIsOriginal(request.getIsOriginal());
            needsRegeneration = true;
        }
        
        // Regenerate outputs if needed
        if (needsRegeneration) {
            Map<String, Object> structuredData = parseStructuredData(content.getStructuredData());
            MultiFormatOutput output = generateMultiFormatOutput(
                structuredData,
                template,
                content.getTitle(),
                content.getCopyrightNotice(),
                content.getContentSource(),
                content.getAuthorName(),
                content.getIsOriginal(),
                request.getFieldOrder()
            );
            
            content.setJsonLd(output.getJsonLd());
            content.setHtmlOutput(output.getHtml());
            content.setMarkdownOutput(output.getMarkdown());
        }
        
        // Update categories
        if (request.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            content.setCategories(categories);
        }
        
        // Update tags
        if (request.getTagNames() != null) {
            Set<Tag> tags = getOrCreateTags(request.getTagNames());
            content.setTags(tags);
        }
        
        content = contentRepository.save(content);
        
        if (content.getStatus() == Content.ContentStatus.PUBLISHED) {
            log.info("Content updated: {}, caches evicted", contentId);
        } else {
            log.info("Content updated: {}", contentId);
        }
        
        return convertToDTO(content);
    }
    
    @Transactional
    public void submitForReview(Long contentId) {
        log.info("Submitting content for review: {}", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        if (content.getStatus() != Content.ContentStatus.DRAFT) {
            throw new ValidationException("只有草稿状态的内容可以提交审核");
        }
        
        content.setStatus(Content.ContentStatus.PENDING_REVIEW);
        content.setSubmittedAt(LocalDateTime.now());
        contentRepository.save(content);
        
        log.info("Content submitted for review: {}", contentId);
    }
    
    @Transactional(readOnly = true)
    public ContentDTO getContent(Long contentId) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        return convertToDTO(content);
    }
    
    @Transactional(readOnly = true)
    public MultiFormatOutput generateOutput(Long contentId) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        MultiFormatOutput output = new MultiFormatOutput();
        output.setJsonLd(content.getJsonLd());
        output.setHtml(content.getHtmlOutput());
        output.setMarkdown(content.getMarkdownOutput());
        
        return output;
    }
    
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "homepage-contents", allEntries = true),
        @CacheEvict(value = "ai-content-jsonld", key = "#contentId"),
        @CacheEvict(value = "ai-search-results", allEntries = true),
        @CacheEvict(value = "ai-sitemap", allEntries = true)
    })
    public void publishContent(Long contentId) {
        log.info("Publishing content: {}", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        if (content.getStatus() != Content.ContentStatus.APPROVED) {
            throw new ValidationException("只有已批准的内容可以发布");
        }
        
        // Validate that at least one category is assigned (Requirement 11.3)
        if (content.getCategories() == null || content.getCategories().isEmpty()) {
            throw new ValidationException("内容必须至少关联一个分类才能发布");
        }
        
        content.setStatus(Content.ContentStatus.PUBLISHED);
        content.setPublishedAt(LocalDateTime.now());
        contentRepository.save(content);
        
        log.info("Content published: {}, caches evicted", contentId);
    }
    
    @Transactional(readOnly = true)
    public Page<ContentDTO> getUserContents(Long userId, Pageable pageable) {
        Page<Content> contents = contentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return contents.map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<ContentDTO> getUserDrafts(Long userId, Pageable pageable) {
        Page<Content> contents = contentRepository.findByUserIdAndStatus(
            userId, Content.ContentStatus.DRAFT, pageable);
        return contents.map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<ContentDTO> getUserPublishedContents(Long userId, Pageable pageable) {
        Page<Content> contents = contentRepository.findByUserIdAndStatus(
            userId, Content.ContentStatus.PUBLISHED, pageable);
        return contents.map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<ContentDTO> getUserPendingContents(Long userId, Pageable pageable) {
        Page<Content> contents = contentRepository.findByUserIdAndStatus(
            userId, Content.ContentStatus.PENDING_REVIEW, pageable);
        return contents.map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<ContentDTO> getPublishedContents(Pageable pageable) {
        log.info("Fetching published contents");
        Page<Content> contents = contentRepository.findByStatus(Content.ContentStatus.PUBLISHED, pageable);
        return contents.map(this::convertToDTO);
    }
    
    /**
     * 搜索已发布内容
     */
    @Transactional(readOnly = true)
    public Page<ContentDTO> searchContents(String keyword, Pageable pageable) {
        log.info("Searching contents with keyword: {}", keyword);
        Page<Content> contents = contentRepository.searchByKeyword(keyword, Content.ContentStatus.PUBLISHED, pageable);
        return contents.map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<ContentDTO> getReviewQueue(Pageable pageable) {
        log.info("Fetching review queue");
        Page<Content> contents = contentRepository.findByStatus(Content.ContentStatus.PENDING_REVIEW, pageable);
        return contents.map(this::convertToDTO);
    }
    
    @Transactional
    public void approveContent(Long contentId, Long adminId) {
        log.info("Approving content: {} by admin: {}", contentId, adminId);
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        if (content.getStatus() != Content.ContentStatus.PENDING_REVIEW) {
            throw new ValidationException("只有待审核状态的内容可以批准");
        }
        
        content.setStatus(Content.ContentStatus.APPROVED);
        content.setReviewedAt(LocalDateTime.now());
        content.setReviewedBy(adminId);
        contentRepository.save(content);
        
        // Send notification to user
        notificationService.createNotification(
            content.getUserId(),
            Notification.NotificationType.CONTENT_APPROVED,
            "您的内容《" + content.getTitle() + "》已通过审核，可以发布了"
        );
        
        log.info("Content approved: {}", contentId);
    }
    
    @Transactional
    public void rejectContent(Long contentId, Long adminId, String reason) {
        log.info("Rejecting content: {} by admin: {}", contentId, adminId);
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        if (content.getStatus() != Content.ContentStatus.PENDING_REVIEW) {
            throw new ValidationException("只有待审核状态的内容可以拒绝");
        }
        
        content.setStatus(Content.ContentStatus.REJECTED);
        content.setReviewedAt(LocalDateTime.now());
        content.setReviewedBy(adminId);
        content.setRejectReason(reason);
        contentRepository.save(content);
        
        // Send notification to user
        String message = "您的内容《" + content.getTitle() + "》审核未通过";
        if (reason != null && !reason.isEmpty()) {
            message += "，原因：" + reason;
        }
        notificationService.createNotification(
            content.getUserId(),
            Notification.NotificationType.CONTENT_REJECTED,
            message
        );
        
        log.info("Content rejected: {}", contentId);
    }
    
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "homepage-contents", allEntries = true),
        @CacheEvict(value = "ai-content-jsonld", key = "#contentId"),
        @CacheEvict(value = "ai-search-results", allEntries = true),
        @CacheEvict(value = "ai-sitemap", allEntries = true)
    })
    public void directPublish(Long contentId, Long adminId) {
        log.info("Direct publishing content: {} by admin: {}", contentId, adminId);
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        if (content.getStatus() != Content.ContentStatus.PENDING_REVIEW && 
            content.getStatus() != Content.ContentStatus.APPROVED) {
            throw new ValidationException("只有待审核或已批准的内容可以直接发布");
        }
        
        content.setStatus(Content.ContentStatus.PUBLISHED);
        content.setReviewedAt(LocalDateTime.now());
        content.setReviewedBy(adminId);
        content.setPublishedAt(LocalDateTime.now());
        contentRepository.save(content);
        
        // Send notification to user
        notificationService.createNotification(
            content.getUserId(),
            Notification.NotificationType.CONTENT_APPROVED,
            "您的内容《" + content.getTitle() + "》已由管理员直接发布"
        );
        
        log.info("Content directly published: {}, caches evicted", contentId);
    }
    
    @Transactional(readOnly = true)
    public IntegrityScore checkIntegrity(Long contentId) {
        log.info("Checking integrity for content: {}", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        Template template = templateRepository.findById(content.getTemplateId())
            .orElseThrow(() -> new ValidationException("模板不存在"));
        
        Map<String, Object> structuredData = parseStructuredData(content.getStructuredData());
        
        return calculateDetailedIntegrityScore(structuredData, template);
    }
    
    @Transactional
    public void updateCopyrightInfo(Long contentId, CopyrightInfoDTO copyrightInfo) {
        log.info("Updating copyright info for content: {}", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        Template template = templateRepository.findById(content.getTemplateId())
            .orElseThrow(() -> new ValidationException("模板不存在"));
        
        content.setCopyrightNotice(copyrightInfo.getCopyrightNotice());
        content.setContentSource(copyrightInfo.getContentSource());
        content.setAuthorName(copyrightInfo.getAuthorName());
        content.setIsOriginal(copyrightInfo.getIsOriginal());
        
        // Regenerate outputs with updated copyright info
        Map<String, Object> structuredData = parseStructuredData(content.getStructuredData());
        MultiFormatOutput output = generateMultiFormatOutput(
            structuredData,
            template,
            content.getTitle(),
            content.getCopyrightNotice(),
            content.getContentSource(),
            content.getAuthorName(),
            content.getIsOriginal(),
            null
        );
        
        content.setJsonLd(output.getJsonLd());
        content.setHtmlOutput(output.getHtml());
        content.setMarkdownOutput(output.getMarkdown());
        
        contentRepository.save(content);
        log.info("Copyright info updated for content: {}", contentId);
    }
    
    @Transactional
    public ContentDTO reorderFields(Long contentId, List<String> fieldOrder) {
        log.info("Reordering fields for content: {}", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        Template template = templateRepository.findById(content.getTemplateId())
            .orElseThrow(() -> new ValidationException("模板不存在"));
        
        Map<String, Object> structuredData = parseStructuredData(content.getStructuredData());
        
        // Regenerate outputs with new field order
        MultiFormatOutput output = generateMultiFormatOutput(
            structuredData,
            template,
            content.getTitle(),
            content.getCopyrightNotice(),
            content.getContentSource(),
            content.getAuthorName(),
            content.getIsOriginal(),
            fieldOrder
        );
        
        content.setJsonLd(output.getJsonLd());
        content.setHtmlOutput(output.getHtml());
        content.setMarkdownOutput(output.getMarkdown());
        
        content = contentRepository.save(content);
        log.info("Fields reordered for content: {}", contentId);
        
        return convertToDTO(content);
    }
    
    private ValidationResult validateStructuredData(Map<String, Object> data, Template template) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        result.setErrors(new ArrayList<>());
        
        try {
            Map<String, Object> schemaDefinition = objectMapper.readValue(
                template.getSchemaDefinition(), 
                Map.class
            );
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = (List<Map<String, Object>>) schemaDefinition.get("fields");
            
            if (fields != null) {
                for (Map<String, Object> field : fields) {
                    String fieldName = (String) field.get("name");
                    Boolean required = (Boolean) field.get("required");
                    
                    if (Boolean.TRUE.equals(required) && !data.containsKey(fieldName)) {
                        result.setValid(false);
                        result.getErrors().add("必填字段缺失: " + fieldName);
                    }
                }
            }
        } catch (Exception e) {
            result.setValid(false);
            result.getErrors().add("模板定义解析失败: " + e.getMessage());
        }
        
        return result;
    }
    
    private BigDecimal calculateIntegrityScore(Map<String, Object> data, Template template) {
        try {
            Map<String, Object> schemaDefinition = objectMapper.readValue(
                template.getSchemaDefinition(), 
                Map.class
            );
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = (List<Map<String, Object>>) schemaDefinition.get("fields");
            
            if (fields == null || fields.isEmpty()) {
                return BigDecimal.ONE;
            }
            
            int totalFields = fields.size();
            int filledFields = 0;
            
            for (Map<String, Object> field : fields) {
                String fieldName = (String) field.get("name");
                if (data.containsKey(fieldName) && data.get(fieldName) != null) {
                    Object value = data.get(fieldName);
                    if (value instanceof String && !((String) value).trim().isEmpty()) {
                        filledFields++;
                    } else if (!(value instanceof String)) {
                        filledFields++;
                    }
                }
            }
            
            double score = (double) filledFields / totalFields;
            return BigDecimal.valueOf(score).setScale(2, BigDecimal.ROUND_HALF_UP);
            
        } catch (Exception e) {
            log.warn("Failed to calculate integrity score: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    private IntegrityScore calculateDetailedIntegrityScore(Map<String, Object> data, Template template) {
        IntegrityScore result = new IntegrityScore();
        result.setMissingFields(new ArrayList<>());
        
        try {
            Map<String, Object> schemaDefinition = objectMapper.readValue(
                template.getSchemaDefinition(), 
                Map.class
            );
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = (List<Map<String, Object>>) schemaDefinition.get("fields");
            
            if (fields == null || fields.isEmpty()) {
                result.setScore(BigDecimal.ONE);
                result.setTotalFields(0);
                result.setFilledFields(0);
                return result;
            }
            
            int totalFields = fields.size();
            int filledFields = 0;
            
            for (Map<String, Object> field : fields) {
                String fieldName = (String) field.get("name");
                boolean isFilled = false;
                
                if (data.containsKey(fieldName) && data.get(fieldName) != null) {
                    Object value = data.get(fieldName);
                    if (value instanceof String && !((String) value).trim().isEmpty()) {
                        isFilled = true;
                    } else if (!(value instanceof String)) {
                        isFilled = true;
                    }
                }
                
                if (isFilled) {
                    filledFields++;
                } else {
                    result.getMissingFields().add(fieldName);
                }
            }
            
            double score = (double) filledFields / totalFields;
            result.setScore(BigDecimal.valueOf(score).setScale(2, BigDecimal.ROUND_HALF_UP));
            result.setTotalFields(totalFields);
            result.setFilledFields(filledFields);
            
        } catch (Exception e) {
            log.warn("Failed to calculate detailed integrity score: {}", e.getMessage());
            result.setScore(BigDecimal.ZERO);
            result.setTotalFields(0);
            result.setFilledFields(0);
        }
        
        return result;
    }
    
    private MultiFormatOutput generateMultiFormatOutput(
        Map<String, Object> structuredData,
        Template template,
        String title,
        String copyrightNotice,
        String contentSource,
        String authorName,
        Boolean isOriginal,
        List<String> fieldOrder
    ) {
        String jsonLd = generateJsonLd(structuredData, template, title, copyrightNotice, contentSource, authorName, isOriginal);
        String html = generateHtml(structuredData, template, title, copyrightNotice, contentSource, authorName, isOriginal, fieldOrder);
        String markdown = generateMarkdown(structuredData, template, title, copyrightNotice, contentSource, authorName, isOriginal, fieldOrder);
        
        return new MultiFormatOutput(jsonLd, html, markdown);
    }
    
    private String generateJsonLd(
        Map<String, Object> structuredData,
        Template template,
        String title,
        String copyrightNotice,
        String contentSource,
        String authorName,
        Boolean isOriginal
    ) {
        Map<String, Object> jsonLd = new LinkedHashMap<>();
        jsonLd.put("@context", "https://schema.org");
        jsonLd.put("@type", template.getSchemaOrgType());
        
        // Add structured data fields
        jsonLd.putAll(structuredData);
        
        // Override with title if provided
        if (title != null && !title.isEmpty()) {
            jsonLd.put("headline", title);
        }
        
        // Add copyright information
        if (authorName != null && !authorName.isEmpty()) {
            Map<String, Object> author = new LinkedHashMap<>();
            author.put("@type", "Person");
            author.put("name", authorName);
            jsonLd.put("author", author);
        }
        
        if (copyrightNotice != null && !copyrightNotice.isEmpty()) {
            jsonLd.put("copyrightNotice", copyrightNotice);
        }
        
        if (contentSource != null && !contentSource.isEmpty()) {
            jsonLd.put("sourceOrganization", contentSource);
        }
        
        if (isOriginal != null) {
            jsonLd.put("isOriginalContent", isOriginal);
        }
        
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonLd);
        } catch (JsonProcessingException e) {
            log.error("Failed to generate JSON-LD: {}", e.getMessage());
            return "{}";
        }
    }
    
    private String generateHtml(
        Map<String, Object> structuredData,
        Template template,
        String title,
        String copyrightNotice,
        String contentSource,
        String authorName,
        Boolean isOriginal,
        List<String> fieldOrder
    ) {
        // Generate JSON-LD first
        String jsonLd = generateJsonLd(structuredData, template, title, copyrightNotice, contentSource, authorName, isOriginal);
        
        // Use HtmlGenerationService to generate AI-friendly HTML
        // Note: contentId will be set after save, so we pass null here and update later if needed
        return htmlGenerationService.generateContentHtml(
            structuredData,
            template,
            title,
            copyrightNotice,
            contentSource,
            authorName,
            isOriginal,
            fieldOrder,
            jsonLd,
            null // contentId will be available after save
        );
    }
    
    private String generateMarkdown(
        Map<String, Object> structuredData,
        Template template,
        String title,
        String copyrightNotice,
        String contentSource,
        String authorName,
        Boolean isOriginal,
        List<String> fieldOrder
    ) {
        StringBuilder md = new StringBuilder();
        md.append("# ").append(title).append("\n\n");
        
        // Add structured data fields
        List<String> keys = fieldOrder != null ? fieldOrder : new ArrayList<>(structuredData.keySet());
        for (String key : keys) {
            if (structuredData.containsKey(key)) {
                Object value = structuredData.get(key);
                md.append("## ").append(key).append("\n\n");
                md.append(value).append("\n\n");
            }
        }
        
        // Add copyright section
        if (authorName != null || copyrightNotice != null || contentSource != null) {
            md.append("---\n\n");
            md.append("## 版权信息\n\n");
            
            if (authorName != null && !authorName.isEmpty()) {
                md.append("**作者：** ").append(authorName).append("\n\n");
            }
            
            if (contentSource != null && !contentSource.isEmpty()) {
                md.append("**来源：** ").append(contentSource).append("\n\n");
            }
            
            if (copyrightNotice != null && !copyrightNotice.isEmpty()) {
                md.append("**版权声明：** ").append(copyrightNotice).append("\n\n");
            }
            
            if (isOriginal != null) {
                md.append("**原创：** ").append(isOriginal ? "是" : "否").append("\n\n");
            }
        }
        
        return md.toString();
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
    
    private Map<String, Object> parseStructuredData(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse structured data: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    private Set<Tag> getOrCreateTags(List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    newTag.setUsageCount(0);
                    return tagRepository.save(newTag);
                });
            
            // Increment usage count
            tag.setUsageCount(tag.getUsageCount() + 1);
            tagRepository.save(tag);
            
            tags.add(tag);
        }
        
        return tags;
    }
    
    public ContentDTO convertToDTO(Content content) {
        ContentDTO dto = new ContentDTO();
        dto.setId(content.getId());
        dto.setUserId(content.getUserId());
        dto.setTemplateId(content.getTemplateId());
        dto.setTitle(content.getTitle());
        dto.setStructuredData(parseStructuredData(content.getStructuredData()));
        dto.setJsonLd(content.getJsonLd());
        dto.setHtmlOutput(content.getHtmlOutput());
        dto.setMarkdownOutput(content.getMarkdownOutput());
        dto.setCopyrightNotice(content.getCopyrightNotice());
        dto.setContentSource(content.getContentSource());
        dto.setAuthorName(content.getAuthorName());
        dto.setIsOriginal(content.getIsOriginal());
        dto.setStatus(content.getStatus());
        dto.setIntegrityScore(content.getIntegrityScore());
        dto.setCreatedAt(content.getCreatedAt());
        dto.setUpdatedAt(content.getUpdatedAt());
        dto.setSubmittedAt(content.getSubmittedAt());
        dto.setReviewedAt(content.getReviewedAt());
        dto.setPublishedAt(content.getPublishedAt());
        dto.setReviewedBy(content.getReviewedBy());
        dto.setRejectReason(content.getRejectReason());
        dto.setViewCount(content.getViewCount());
        
        if (content.getCategories() != null) {
            dto.setCategoryIds(content.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList()));
        }
        
        if (content.getTags() != null) {
            dto.setTagNames(content.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    @Transactional
    public void incrementViewCount(Long contentId) {
        log.debug("Incrementing view count for content: {}", contentId);
        contentRepository.findById(contentId).ifPresent(content -> {
            Integer currentCount = content.getViewCount();
            content.setViewCount(currentCount != null ? currentCount + 1 : 1);
            contentRepository.save(content);
        });
    }
    
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "publishedContents", allEntries = true),
        @CacheEvict(value = "contentsByCategory", allEntries = true)
    })
    public void deleteContent(Long contentId) {
        log.info("Deleting content: {}", contentId);
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ValidationException("内容不存在"));
        
        // 只允许删除草稿和已拒绝的内容
        if (content.getStatus() != Content.ContentStatus.DRAFT && 
            content.getStatus() != Content.ContentStatus.REJECTED) {
            throw new ValidationException("只能删除草稿或已拒绝的内容");
        }
        
        contentRepository.delete(content);
        log.info("Content deleted: {}", contentId);
    }
}
