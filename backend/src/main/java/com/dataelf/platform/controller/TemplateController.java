package com.dataelf.platform.controller;

import com.dataelf.platform.dto.*;
import com.dataelf.platform.service.TemplateService;
import com.dataelf.platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {
    
    private final TemplateService templateService;
    private final JwtUtil jwtUtil;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTemplates() {
        List<TemplateDTO> templates = templateService.getAllTemplates();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", templates);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTemplate(@PathVariable Long id) {
        TemplateDTO template = templateService.getTemplate(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", template);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTemplate(
            @Valid @RequestBody TemplateCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        
        TemplateDTO template = templateService.createTemplate(request, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", template);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTemplate(
            @PathVariable Long id,
            @RequestBody TemplateUpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        TemplateDTO template = templateService.updateTemplate(id, request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", template);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        templateService.deleteTemplate(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "模板删除成功");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/export")
    public ResponseEntity<Map<String, Object>> exportTemplate(@PathVariable Long id) {
        String json = templateService.exportTemplate(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", json);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importTemplate(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        
        String jsonContent = request.get("jsonContent");
        TemplateDTO template = templateService.importTemplate(jsonContent, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", template);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateSchema(@RequestBody Map<String, String> request) {
        String schemaDefinition = request.get("schemaDefinition");
        String schemaOrgType = request.get("schemaOrgType");
        
        ValidationResult result = templateService.validateSchema(schemaDefinition, schemaOrgType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isValid());
        response.put("data", result);
        return ResponseEntity.ok(response);
    }
}
