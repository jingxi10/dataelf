package com.dataelf.platform.service;

import com.dataelf.platform.dto.*;
import com.dataelf.platform.entity.Template;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.TemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TemplateServiceTest {
    
    @Autowired
    private TemplateService templateService;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        // System templates are initialized automatically via @PostConstruct
    }
    
    @Test
    void testSystemTemplatesInitialized() {
        List<Template> systemTemplates = templateRepository.findByIsSystem(true);
        assertEquals(6, systemTemplates.size(), "应该有6个系统预设模板");
        
        List<String> expectedTypes = List.of("TechArticle", "CaseStudy", "DataReport", 
                                             "ProductReview", "SurveyReport", "IndustryAnalysis");
        
        for (String type : expectedTypes) {
            assertTrue(systemTemplates.stream().anyMatch(t -> t.getType().equals(type)),
                      "应该包含" + type + "模板");
        }
    }
    
    @Test
    void testGetAllTemplates() {
        List<TemplateDTO> templates = templateService.getAllTemplates();
        assertTrue(templates.size() >= 6, "至少应该有6个模板");
    }
    
    @Test
    void testGetTemplate() {
        List<Template> systemTemplates = templateRepository.findByIsSystem(true);
        assertFalse(systemTemplates.isEmpty());
        
        Long templateId = systemTemplates.get(0).getId();
        TemplateDTO template = templateService.getTemplate(templateId);
        
        assertNotNull(template);
        assertEquals(templateId, template.getId());
    }
    
    @Test
    void testCreateTemplate_Valid() {
        TemplateCreateRequest request = new TemplateCreateRequest();
        request.setName("测试模板");
        request.setType("TestType");
        request.setDescription("测试描述");
        request.setSchemaOrgType("Article");
        request.setSchemaDefinition("""
            {
              "@context": "https://schema.org",
              "@type": "Article",
              "fields": [
                {"name": "headline", "type": "string", "required": true}
              ]
            }
            """);
        
        TemplateDTO created = templateService.createTemplate(request, 1L);
        
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("测试模板", created.getName());
        assertEquals("TestType", created.getType());
        assertFalse(created.getIsSystem());
        assertEquals(1L, created.getCreatedBy());
    }
    
    @Test
    void testCreateTemplate_MissingContext() {
        TemplateCreateRequest request = new TemplateCreateRequest();
        request.setName("无效模板");
        request.setType("InvalidType");
        request.setSchemaOrgType("Article");
        request.setSchemaDefinition("""
            {
              "@type": "Article",
              "fields": []
            }
            """);
        
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            templateService.createTemplate(request, 1L);
        });
        
        assertTrue(exception.getMessage().contains("@context"));
    }
    
    @Test
    void testCreateTemplate_MissingType() {
        TemplateCreateRequest request = new TemplateCreateRequest();
        request.setName("无效模板");
        request.setType("InvalidType");
        request.setSchemaOrgType("Article");
        request.setSchemaDefinition("""
            {
              "@context": "https://schema.org",
              "fields": []
            }
            """);
        
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            templateService.createTemplate(request, 1L);
        });
        
        assertTrue(exception.getMessage().contains("@type"));
    }
    
    @Test
    void testUpdateTemplate() {
        // Create a template first
        TemplateCreateRequest createRequest = new TemplateCreateRequest();
        createRequest.setName("原始模板");
        createRequest.setType("OriginalType");
        createRequest.setSchemaOrgType("Article");
        createRequest.setSchemaDefinition("""
            {
              "@context": "https://schema.org",
              "@type": "Article",
              "fields": []
            }
            """);
        
        TemplateDTO created = templateService.createTemplate(createRequest, 1L);
        
        // Update it
        TemplateUpdateRequest updateRequest = new TemplateUpdateRequest();
        updateRequest.setName("更新后的模板");
        updateRequest.setDescription("新描述");
        
        TemplateDTO updated = templateService.updateTemplate(created.getId(), updateRequest);
        
        assertEquals("更新后的模板", updated.getName());
        assertEquals("新描述", updated.getDescription());
    }
    
    @Test
    void testUpdateSystemTemplate_ShouldFail() {
        List<Template> systemTemplates = templateRepository.findByIsSystem(true);
        assertFalse(systemTemplates.isEmpty());
        
        Long systemTemplateId = systemTemplates.get(0).getId();
        TemplateUpdateRequest updateRequest = new TemplateUpdateRequest();
        updateRequest.setName("尝试修改系统模板");
        
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            templateService.updateTemplate(systemTemplateId, updateRequest);
        });
        
        assertTrue(exception.getMessage().contains("系统模板不能修改"));
    }
    
    @Test
    void testDeleteTemplate() {
        // Create a template
        TemplateCreateRequest request = new TemplateCreateRequest();
        request.setName("待删除模板");
        request.setType("ToDelete");
        request.setSchemaOrgType("Article");
        request.setSchemaDefinition("""
            {
              "@context": "https://schema.org",
              "@type": "Article",
              "fields": []
            }
            """);
        
        TemplateDTO created = templateService.createTemplate(request, 1L);
        
        // Delete it
        templateService.deleteTemplate(created.getId());
        
        // Verify it's deleted
        assertThrows(ValidationException.class, () -> {
            templateService.getTemplate(created.getId());
        });
    }
    
    @Test
    void testDeleteSystemTemplate_ShouldFail() {
        List<Template> systemTemplates = templateRepository.findByIsSystem(true);
        assertFalse(systemTemplates.isEmpty());
        
        Long systemTemplateId = systemTemplates.get(0).getId();
        
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            templateService.deleteTemplate(systemTemplateId);
        });
        
        assertTrue(exception.getMessage().contains("系统模板不能删除"));
    }
    
    @Test
    void testExportTemplate() throws Exception {
        List<Template> systemTemplates = templateRepository.findByIsSystem(true);
        assertFalse(systemTemplates.isEmpty());
        
        Long templateId = systemTemplates.get(0).getId();
        String exported = templateService.exportTemplate(templateId);
        
        assertNotNull(exported);
        assertTrue(exported.contains("\"name\""));
        assertTrue(exported.contains("\"schemaDefinition\""));
        
        // Verify it's valid JSON
        TemplateDTO parsed = objectMapper.readValue(exported, TemplateDTO.class);
        assertNotNull(parsed);
    }
    
    @Test
    void testImportTemplate() {
        String jsonContent = """
            {
              "name": "导入的模板",
              "type": "ImportedType",
              "description": "导入测试",
              "schemaOrgType": "Article",
              "schemaDefinition": "{\\"@context\\":\\"https://schema.org\\",\\"@type\\":\\"Article\\",\\"fields\\":[]}"
            }
            """;
        
        TemplateDTO imported = templateService.importTemplate(jsonContent, 1L);
        
        assertNotNull(imported);
        assertNotNull(imported.getId());
        assertEquals("导入的模板", imported.getName());
        assertEquals("ImportedType", imported.getType());
        assertFalse(imported.getIsSystem());
    }
    
    @Test
    void testExportImportRoundTrip() throws Exception {
        // Create a template
        TemplateCreateRequest request = new TemplateCreateRequest();
        request.setName("往返测试模板");
        request.setType("RoundTripType");
        request.setDescription("测试导入导出往返");
        request.setSchemaOrgType("Article");
        request.setSchemaDefinition("""
            {
              "@context": "https://schema.org",
              "@type": "Article",
              "fields": [
                {"name": "headline", "type": "string", "required": true},
                {"name": "author", "type": "Person", "required": true}
              ]
            }
            """);
        
        TemplateDTO original = templateService.createTemplate(request, 1L);
        
        // Export it
        String exported = templateService.exportTemplate(original.getId());
        
        // Import it
        TemplateDTO imported = templateService.importTemplate(exported, 1L);
        
        // Verify equivalence
        assertEquals(original.getName(), imported.getName());
        assertEquals(original.getType(), imported.getType());
        assertEquals(original.getDescription(), imported.getDescription());
        assertEquals(original.getSchemaOrgType(), imported.getSchemaOrgType());
        
        // Parse and compare schema definitions
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(
            mapper.readTree(original.getSchemaDefinition()),
            mapper.readTree(imported.getSchemaDefinition())
        );
    }
    
    @Test
    void testValidateSchema_Valid() {
        String schemaDefinition = """
            {
              "@context": "https://schema.org",
              "@type": "Article",
              "fields": []
            }
            """;
        
        ValidationResult result = templateService.validateSchema(schemaDefinition, "Article");
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }
    
    @Test
    void testValidateSchema_MissingContext() {
        String schemaDefinition = """
            {
              "@type": "Article",
              "fields": []
            }
            """;
        
        ValidationResult result = templateService.validateSchema(schemaDefinition, "Article");
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("@context")));
    }
    
    @Test
    void testValidateSchema_MissingType() {
        String schemaDefinition = """
            {
              "@context": "https://schema.org",
              "fields": []
            }
            """;
        
        ValidationResult result = templateService.validateSchema(schemaDefinition, "Article");
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("@type")));
    }
    
    @Test
    void testValidateSchema_TypeMismatch() {
        String schemaDefinition = """
            {
              "@context": "https://schema.org",
              "@type": "Article",
              "fields": []
            }
            """;
        
        ValidationResult result = templateService.validateSchema(schemaDefinition, "Product");
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("不匹配")));
    }
}
