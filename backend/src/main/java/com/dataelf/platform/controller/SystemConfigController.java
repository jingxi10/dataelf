package com.dataelf.platform.controller;

import com.dataelf.platform.dto.SystemConfigUpdateRequest;
import com.dataelf.platform.service.SystemConfigService;
import com.dataelf.platform.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "系统配置", description = "系统配置管理接口")
public class SystemConfigController {
    
    private final SystemConfigService systemConfigService;
    private final JwtUtil jwtUtil;
    
    @GetMapping("/public/config")
    @Operation(summary = "获取公开配置", description = "获取网站公开配置信息（无需认证）")
    public ResponseEntity<Map<String, Object>> getPublicConfig() {
        Map<String, Object> config = systemConfigService.getPublicConfigs();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", config);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin/config")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "获取所有配置", description = "获取所有系统配置（管理员）")
    public ResponseEntity<Map<String, Object>> getAllConfig(
            HttpServletRequest httpRequest
    ) {
        // 验证管理员权限
        getUserIdFromToken(httpRequest);
        
        Map<String, String> config = systemConfigService.getAllConfigs();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", config);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/admin/config")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "更新配置", description = "更新系统配置（管理员）")
    public ResponseEntity<Map<String, Object>> updateConfig(
            HttpServletRequest httpRequest,
            @Valid @RequestBody SystemConfigUpdateRequest request
    ) {
        // 验证管理员权限
        getUserIdFromToken(httpRequest);
        
        systemConfigService.setConfigValue(
            request.getConfigKey(),
            request.getConfigValue(),
            request.getDescription()
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "配置更新成功");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/admin/config/logo")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "上传Logo", description = "上传网站Logo图片（管理员）")
    public ResponseEntity<Map<String, Object>> uploadLogo(
            HttpServletRequest httpRequest,
            @RequestParam("file") MultipartFile file
    ) {
        // 验证管理员权限
        getUserIdFromToken(httpRequest);
        
        try {
            String logoUrl = systemConfigService.uploadLogo(file);
            
            Map<String, Object> result = new HashMap<>();
            result.put("logoUrl", logoUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("message", "Logo上传成功");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload logo", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "UPLOAD_FAILED");
            response.put("message", "Logo上传失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未找到有效的认证令牌");
    }
}
