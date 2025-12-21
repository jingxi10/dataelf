package com.dataelf.platform.service;

import com.dataelf.platform.entity.Content;
import com.dataelf.platform.entity.Template;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.ContentRepository;
import com.dataelf.platform.repository.TemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExportServiceTest {
    
    @Autowired
    private ExportService exportService;
    
    @Autowired
    private ContentRepository contentRepository;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Long testContentId;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create a test template
        Template template = new Template();
        template.setName("测试文章模板");
        template.setType("Article");
        template.setSchemaOrgType("Article");
        
        Map<String, Object> schemaDefinition = new HashMap<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "headline");
        field1.put("type", "text");
        field1.put("required", true);
        
        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "articleBody");
        field2.put("type", "textarea");
        field2.put("required", true);
        
        schemaDefinition.put("fields", new Object[]{field1, field2});
        template.setSchemaDefinition(objectMapper.writeValueAsString(schemaDefinition));
        template.setIsSystem(false);
        template = templateRepository.save(template);
        
        // Create test content
        Content content = new Content();
        content.setUserId(1L);
        content.setTemplateId(template.getId());
        content.setTitle("测试文章标题");
        
        Map<String, Object> structuredData = new HashMap<>();
        structuredData.put("headline", "测试文章标题");
        structuredData.put("articleBody", "这是测试文章的正文内容");
        content.setStructuredData(objectMapper.writeValueAsString(structuredData));
        
        // Generate JSON-LD
        Map<String, Object> jsonLd = new HashMap<>();
        jsonLd.put("@context", "https://schema.org");
        jsonLd.put("@type", "Article");
        jsonLd.put("headline", "测试文章标题");
        jsonLd.put("articleBody", "这是测试文章的正文内容");
        content.setJsonLd(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonLd));
        
        // Generate HTML with semantic markup
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <script type=\"application/ld+json\">\n" +
                content.getJsonLd() + "\n" +
                "  </script>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <main itemscope itemtype=\"https://schema.org/Article\">\n" +
                "    <h1 itemprop=\"headline\">测试文章标题</h1>\n" +
                "    <div itemprop=\"articleBody\">这是测试文章的正文内容</div>\n" +
                "  </main>\n" +
                "</body>\n" +
                "</html>";
        content.setHtmlOutput(html);
        
        // Generate Markdown
        String markdown = "# 测试文章标题\n\n## headline\n\n测试文章标题\n\n## articleBody\n\n这是测试文章的正文内容\n\n";
        content.setMarkdownOutput(markdown);
        
        content.setCopyrightNotice("版权所有");
        content.setAuthorName("测试作者");
        content.setContentSource("测试来源");
        content.setIsOriginal(true);
        content.setStatus(Content.ContentStatus.PUBLISHED);
        content.setIntegrityScore(BigDecimal.valueOf(1.0));
        
        content = contentRepository.save(content);
        testContentId = content.getId();
    }
    
    @Test
    void testExportAsJsonLd_Success() {
        String jsonLd = exportService.exportAsJsonLd(testContentId);
        
        assertNotNull(jsonLd);
        assertTrue(jsonLd.contains("@context"));
        assertTrue(jsonLd.contains("@type"));
        assertTrue(jsonLd.contains("schema.org"));
        assertTrue(jsonLd.contains("测试文章标题"));
    }
    
    @Test
    void testExportAsJsonLd_InvalidContent() {
        Exception exception = assertThrows(Exception.class, () -> {
            exportService.exportAsJsonLd(99999L);
        });
        
        // Check if it's ValidationException or RuntimeException wrapping ValidationException
        assertTrue(exception instanceof ValidationException || 
                  (exception.getCause() instanceof ValidationException),
                  "Expected ValidationException but got: " + exception.getClass());
    }
    
    @Test
    void testExportAsHtml_Success() {
        String html = exportService.exportAsHtml(testContentId);
        
        assertNotNull(html);
        assertTrue(html.contains("itemscope"));
        assertTrue(html.contains("itemtype"));
        assertTrue(html.contains("itemprop"));
        assertTrue(html.contains("schema.org"));
        assertTrue(html.contains("测试文章标题"));
    }
    
    @Test
    void testExportAsHtml_InvalidContent() {
        Exception exception = assertThrows(Exception.class, () -> {
            exportService.exportAsHtml(99999L);
        });
        
        // Check if it's ValidationException or RuntimeException wrapping ValidationException
        assertTrue(exception instanceof ValidationException || 
                  (exception.getCause() instanceof ValidationException),
                  "Expected ValidationException but got: " + exception.getClass());
    }
    
    @Test
    void testExportAsMarkdown_Success() {
        String markdown = exportService.exportAsMarkdown(testContentId);
        
        assertNotNull(markdown);
        assertTrue(markdown.contains("#"));
        assertTrue(markdown.contains("测试文章标题"));
        assertTrue(markdown.contains("这是测试文章的正文内容"));
    }
    
    @Test
    void testExportAsMarkdown_InvalidContent() {
        Exception exception = assertThrows(Exception.class, () -> {
            exportService.exportAsMarkdown(99999L);
        });
        
        // Check if it's ValidationException or RuntimeException wrapping ValidationException
        assertTrue(exception instanceof ValidationException || 
                  (exception.getCause() instanceof ValidationException),
                  "Expected ValidationException but got: " + exception.getClass());
    }
    
    @Test
    void testExportAsCsv_Success() {
        byte[] csv = exportService.exportAsCsv(testContentId);
        
        assertNotNull(csv);
        assertTrue(csv.length > 0);
        
        String csvContent = new String(csv, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("headline"));
        assertTrue(csvContent.contains("articleBody"));
        assertTrue(csvContent.contains("测试文章标题"));
        assertTrue(csvContent.contains("这是测试文章的正文内容"));
        assertTrue(csvContent.contains("元数据"));
        assertTrue(csvContent.contains("作者"));
        assertTrue(csvContent.contains("测试作者"));
    }
    
    @Test
    void testExportAsCsv_InvalidContent() {
        Exception exception = assertThrows(Exception.class, () -> {
            exportService.exportAsCsv(99999L);
        });
        
        // Check if it's ValidationException or RuntimeException wrapping ValidationException
        assertTrue(exception instanceof ValidationException || 
                  (exception.getCause() instanceof ValidationException),
                  "Expected ValidationException but got: " + exception.getClass());
    }
    
    @Test
    void testExportAsCsv_WithCommasInValues() throws Exception {
        // Create content with commas in values
        Content content = contentRepository.findById(testContentId).orElseThrow();
        
        Map<String, Object> structuredData = new HashMap<>();
        structuredData.put("headline", "标题,包含,逗号");
        structuredData.put("articleBody", "正文内容");
        content.setStructuredData(objectMapper.writeValueAsString(structuredData));
        contentRepository.save(content);
        
        byte[] csv = exportService.exportAsCsv(testContentId);
        String csvContent = new String(csv, StandardCharsets.UTF_8);
        
        // Values with commas should be quoted
        assertTrue(csvContent.contains("\"标题,包含,逗号\""));
    }
    
    @Test
    void testJsonLdValidation_MissingContext() {
        Content content = contentRepository.findById(testContentId).orElseThrow();
        content.setJsonLd("{\"@type\": \"Article\"}");
        contentRepository.save(content);
        
        Exception exception = assertThrows(Exception.class, () -> {
            exportService.exportAsJsonLd(testContentId);
        });
        
        // Get the actual ValidationException (might be wrapped)
        Throwable actualException = exception instanceof ValidationException ? 
                                   exception : exception.getCause();
        assertTrue(actualException instanceof ValidationException);
        assertTrue(actualException.getMessage().contains("@context"));
    }
    
    @Test
    void testJsonLdValidation_MissingType() {
        Content content = contentRepository.findById(testContentId).orElseThrow();
        content.setJsonLd("{\"@context\": \"https://schema.org\"}");
        contentRepository.save(content);
        
        Exception exception = assertThrows(Exception.class, () -> {
            exportService.exportAsJsonLd(testContentId);
        });
        
        // Get the actual ValidationException (might be wrapped)
        Throwable actualException = exception instanceof ValidationException ? 
                                   exception : exception.getCause();
        assertTrue(actualException instanceof ValidationException);
        assertTrue(actualException.getMessage().contains("@type"));
    }
    
    @Test
    void testJsonLdValidation_InvalidSchemaOrg() {
        Content content = contentRepository.findById(testContentId).orElseThrow();
        content.setJsonLd("{\"@context\": \"http://example.com\", \"@type\": \"Article\"}");
        contentRepository.save(content);
        
        Exception exception = assertThrows(Exception.class, () -> {
            exportService.exportAsJsonLd(testContentId);
        });
        
        // Get the actual ValidationException (might be wrapped)
        Throwable actualException = exception instanceof ValidationException ? 
                                   exception : exception.getCause();
        assertTrue(actualException instanceof ValidationException);
        assertTrue(actualException.getMessage().contains("schema.org"));
    }
    
    @Test
    void testHtmlValidation_MissingSemanticMarkup() {
        Content content = contentRepository.findById(testContentId).orElseThrow();
        content.setHtmlOutput("<html><body><h1>Title</h1></body></html>");
        contentRepository.save(content);
        
        Exception exception = assertThrows(Exception.class, () -> {
            exportService.exportAsHtml(testContentId);
        });
        
        // Get the actual ValidationException (might be wrapped)
        Throwable actualException = exception instanceof ValidationException ? 
                                   exception : exception.getCause();
        assertTrue(actualException instanceof ValidationException);
        assertTrue(actualException.getMessage().contains("语义化标记"));
    }
    
    @Test
    void testMarkdownValidation_MissingHeading() {
        Content content = contentRepository.findById(testContentId).orElseThrow();
        content.setMarkdownOutput("Just plain text without headings");
        contentRepository.save(content);
        
        Exception exception = assertThrows(Exception.class, () -> {
            exportService.exportAsMarkdown(testContentId);
        });
        
        // Get the actual ValidationException (might be wrapped)
        Throwable actualException = exception instanceof ValidationException ? 
                                   exception : exception.getCause();
        assertTrue(actualException instanceof ValidationException);
        assertTrue(actualException.getMessage().contains("标题标记"));
    }
}
