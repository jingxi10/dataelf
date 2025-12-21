package com.dataelf.platform.controller;

import com.dataelf.platform.dto.LoginRequest;
import com.dataelf.platform.dto.LoginResponse;
import com.dataelf.platform.dto.RegisterRequest;
import com.dataelf.platform.dto.UserDTO;
import com.dataelf.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "认证", description = "用户注册和登录接口")
public class AuthController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    @Operation(
            summary = "用户注册",
            description = """
                    注册新用户账号。
                    
                    **验证规则：**
                    - 邮箱：必须是有效的邮箱格式
                    - 手机号：必须是有效的手机号格式
                    - 密码：至少8个字符，包含大小写字母和数字
                    
                    **注册流程：**
                    1. 提交注册信息
                    2. 系统创建待审核账号（状态：PENDING）
                    3. 等待管理员审核
                    4. 审核通过后收到邮件通知
                    
                    **无需认证**
                    """,
            security = @SecurityRequirement(name = "none")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "注册成功",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "注册成功，等待审核，请留意查看邮箱",
                                      "data": {
                                        "id": 1,
                                        "email": "user@example.com",
                                        "phone": "13800138000",
                                        "status": "PENDING",
                                        "createdAt": "2024-01-01T12:00:00Z"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "验证失败",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "VALIDATION_ERROR",
                                        "message": "输入验证失败",
                                        "details": [
                                          {
                                            "field": "email",
                                            "message": "邮箱格式无效"
                                          }
                                        ]
                                      }
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        
        UserDTO user = userService.register(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "注册成功，等待审核，请留意查看邮箱");
        response.put("data", user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    @Operation(
            summary = "用户登录",
            description = """
                    使用邮箱和密码登录系统。
                    
                    **登录要求：**
                    - 账号状态必须是 APPROVED（已审核）
                    - 账号未过期
                    - 密码正确
                    
                    **安全机制：**
                    - 5次登录失败后锁定15分钟
                    - 返回JWT令牌用于后续认证
                    
                    **无需认证**
                    """,
            security = @SecurityRequirement(name = "none")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "登录成功",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "登录成功",
                                      "data": {
                                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "user": {
                                          "id": 1,
                                          "email": "user@example.com",
                                          "role": "USER",
                                          "expiresAt": "2024-02-01T12:00:00Z"
                                        }
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "认证失败",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "AUTHENTICATION_ERROR",
                                        "message": "邮箱或密码错误"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "账号状态异常",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "ACCOUNT_PENDING",
                                        "message": "账号审核中"
                                      }
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("Login request received for email: {}", request.getEmail());
        
        String ipAddress = getClientIpAddress(httpRequest);
        LoginResponse loginResponse = userService.login(request, ipAddress);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登录成功");
        response.put("data", loginResponse);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    @GetMapping("/me")
    @Operation(
            summary = "获取当前用户信息",
            description = "获取当前登录用户的详细信息",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "获取成功"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "未认证"
            )
    })
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("code", "UNAUTHORIZED", "message", "未登录"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            token = token.substring(7);
            UserDTO user = userService.getUserFromToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get current user", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of("code", "UNAUTHORIZED", "message", "Token无效或已过期"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    @PostMapping("/logout")
    @Operation(
            summary = "用户登出",
            description = "用户登出，清除会话信息"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "登出成功"
            )
    })
    public ResponseEntity<Map<String, Object>> logout() {
        // JWT是无状态的，客户端只需删除token即可
        // 这里只返回成功响应
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        
        return ResponseEntity.ok(response);
    }
}
