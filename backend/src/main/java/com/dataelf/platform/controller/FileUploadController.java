package com.dataelf.platform.controller;

import com.dataelf.platform.service.FileUploadService;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "文件上传", description = "文件上传接口")
@SecurityRequirement(name = "bearerAuth")
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    private final JwtUtil jwtUtil;
    
    /**
     * 上传图片
     */
    @PostMapping("/image")
    @Operation(summary = "上传图片", description = "上传图片文件，支持 JPG、PNG、GIF、WebP、SVG 格式")
    public ResponseEntity<Map<String, Object>> uploadImage(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file
    ) {
        Long userId = getUserIdFromRequest(request);
        
        try {
            String url = fileUploadService.uploadImage(file, userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", file.getOriginalFilename());
            data.put("size", file.getSize());
            data.put("type", file.getContentType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("message", "图片上传成功");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload image", e);
            return buildErrorResponse("图片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传视频
     */
    @PostMapping("/video")
    @Operation(summary = "上传视频", description = "上传视频文件，支持 MP4、WebM、OGG 格式")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file
    ) {
        Long userId = getUserIdFromRequest(request);
        
        try {
            String url = fileUploadService.uploadVideo(file, userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", file.getOriginalFilename());
            data.put("size", file.getSize());
            data.put("type", file.getContentType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("message", "视频上传成功");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload video", e);
            return buildErrorResponse("视频上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传文件（自动识别类型）
     */
    @PostMapping("/file")
    @Operation(summary = "上传文件", description = "上传文件，自动识别类型（图片、视频、文档）")
    public ResponseEntity<Map<String, Object>> uploadFile(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file
    ) {
        Long userId = getUserIdFromRequest(request);
        
        try {
            String url = fileUploadService.uploadFile(file, userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", file.getOriginalFilename());
            data.put("size", file.getSize());
            data.put("type", file.getContentType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("message", "文件上传成功");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            return buildErrorResponse("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping
    @Operation(summary = "删除文件", description = "删除已上传的文件")
    public ResponseEntity<Map<String, Object>> deleteFile(
            HttpServletRequest request,
            @RequestParam("url") String fileUrl
    ) {
        getUserIdFromRequest(request); // 验证登录
        
        boolean deleted = fileUploadService.deleteFile(fileUrl);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("message", deleted ? "文件删除成功" : "文件删除失败");
        
        return ResponseEntity.ok(response);
    }
    
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未授权");
    }
    
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", Map.of("code", "UPLOAD_FAILED", "message", message));
        return ResponseEntity.badRequest().body(response);
    }
}
