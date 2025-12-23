package com.dataelf.platform.controller;

import com.dataelf.platform.dto.ApproveUserRequest;
import com.dataelf.platform.dto.CreateAdminRequest;
import com.dataelf.platform.dto.ExtendAccountRequest;
import com.dataelf.platform.dto.UserDTO;
import com.dataelf.platform.entity.User;
import com.dataelf.platform.repository.UserRepository;
import com.dataelf.platform.service.UserService;
import com.dataelf.platform.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "管理员", description = "管理员操作接口 - 需要JWT认证和管理员权限")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final com.dataelf.platform.service.ContentService contentService;
    private final JwtUtil jwtUtil;
    
    @Value("${app.password.bcrypt-strength:10}")
    private int bcryptStrength;
    
    private BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
    
    private Long getAdminIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未找到有效的认证令牌");
    }
    
    @PostMapping("/users/create-admin")
    @Operation(summary = "新增管理员用户", description = "创建一个新的管理员账号")
    public ResponseEntity<Map<String, Object>> createAdmin(
            @Valid @RequestBody CreateAdminRequest request,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        log.info("Admin {} creating new admin user with email: {}", adminId, request.getEmail());
        
        // 检查邮箱是否已存在
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "该邮箱已被注册");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 创建管理员用户
        User admin = new User();
        admin.setEmail(request.getEmail());
        admin.setPhone(request.getPhone());
        admin.setPasswordHash(passwordEncoder().encode(request.getPassword()));
        admin.setRole(User.UserRole.ADMIN);
        admin.setStatus(User.UserStatus.APPROVED);
        admin.setApprovedAt(LocalDateTime.now());
        admin.setApprovedBy(adminId);
        admin.setExpiresAt(LocalDateTime.now().plusDays(31)); // 默认31天有效期
        
        User savedAdmin = userRepository.save(admin);
        log.info("New admin user created with id: {}", savedAdmin.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "管理员用户创建成功");
        response.put("data", UserDTO.fromEntity(savedAdmin));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/users/approve")
    @Operation(
            summary = "批准用户账号",
            description = """
                    管理员批准用户注册申请并设置账号有效期。
                    
                    **操作流程：**
                    1. 管理员审核用户注册信息
                    2. 设置账号有效天数
                    3. 系统更新用户状态为APPROVED
                    4. 发送批准通知邮件和站内通知
                    
                    **需要认证：** JWT令牌 + 管理员权限
                    
                    **验证需求：** 1.3, 5.2
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "批准成功",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "用户账号已批准"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "未认证"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "无管理员权限"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "用户不存在"
            )
    })
    public ResponseEntity<Map<String, Object>> approveUser(
            @Valid @RequestBody ApproveUserRequest request,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        log.info("Admin {} approving user {}", adminId, request.getUserId());
        
        userService.approveUser(request.getUserId(), adminId, request.getValidDays());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "用户账号已批准");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/users/extend")
    @Operation(summary = "延长账号时长", description = "管理员延长用户账号的有效期")
    public ResponseEntity<Map<String, Object>> extendAccount(
            @Valid @RequestBody ExtendAccountRequest request,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        log.info("Admin {} extending account for user {} by {} days", 
                adminId, request.getUserId(), request.getDays());
        
        userService.extendAccount(request.getUserId(), request.getDays());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "账号时长已延长");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users/{userId}")
    @Operation(summary = "查看用户详情", description = "管理员查看用户账号详细信息")
    public ResponseEntity<Map<String, Object>> getUserDetails(@PathVariable Long userId) {
        log.info("Admin fetching details for user {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        UserDTO userDTO = UserDTO.fromEntity(user);
        
        // 计算剩余天数
        Long remainingDays = null;
        if (user.getExpiresAt() != null) {
            remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), user.getExpiresAt());
            if (remainingDays < 0) {
                remainingDays = 0L;
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("user", userDTO);
        data.put("remainingDays", remainingDays);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "管理员查看用户列表，支持分页、搜索和筛选")
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        log.info("Admin fetching user list, page: {}, size: {}, status: {}, search: {}", 
                page, size, status, search);
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.by("createdAt").descending());
        
        org.springframework.data.domain.Page<User> userPage;
        
        if (status != null && !status.isEmpty()) {
            User.UserStatus userStatus = User.UserStatus.valueOf(status);
            if (search != null && !search.isEmpty()) {
                userPage = userRepository.findByStatusAndEmailContainingOrPhoneContaining(
                    userStatus, search, search, pageable);
            } else {
                userPage = userRepository.findByStatus(userStatus, pageable);
            }
        } else if (search != null && !search.isEmpty()) {
            userPage = userRepository.findByEmailContainingOrPhoneContaining(
                search, search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of(
            "content", userDTOs,
            "totalElements", userPage.getTotalElements(),
            "totalPages", userPage.getTotalPages(),
            "currentPage", userPage.getNumber(),
            "size", userPage.getSize()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users/pending")
    @Operation(summary = "获取待审核用户列表", description = "管理员查看所有待审核的用户")
    public ResponseEntity<Map<String, Object>> getPendingUsers() {
        log.info("Admin fetching pending users");
        
        List<User> pendingUsers = userRepository.findByStatus(User.UserStatus.PENDING);
        List<UserDTO> userDTOs = pendingUsers.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", userDTOs);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users/expiring")
    @Operation(summary = "获取即将到期的用户", description = "管理员查看7天内即将到期的用户")
    public ResponseEntity<Map<String, Object>> getExpiringUsers() {
        log.info("Admin fetching expiring users");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);
        
        List<User> expiringUsers = userRepository.findByExpiresAtBetween(now, sevenDaysLater);
        List<UserDTO> userDTOs = expiringUsers.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", userDTOs);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/content/review-queue")
    @Operation(summary = "获取审核队列", description = "管理员查看待审核的内容列表")
    public ResponseEntity<Map<String, Object>> getReviewQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin fetching review queue, page: {}, size: {}", page, size);
        
        org.springframework.data.domain.Page<com.dataelf.platform.dto.ContentDTO> reviewQueue = 
            contentService.getReviewQueue(org.springframework.data.domain.PageRequest.of(page, size));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", reviewQueue.getContent());
        response.put("totalPages", reviewQueue.getTotalPages());
        response.put("totalElements", reviewQueue.getTotalElements());
        response.put("currentPage", reviewQueue.getNumber());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/content/{contentId}/approve")
    @Operation(summary = "批准内容", description = "管理员批准待审核的内容")
    public ResponseEntity<Map<String, Object>> approveContent(
            @PathVariable Long contentId,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        log.info("Admin {} approving content {}", adminId, contentId);
        
        contentService.approveContent(contentId, adminId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "内容已批准");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/content/{contentId}/reject")
    @Operation(summary = "拒绝内容", description = "管理员拒绝待审核的内容")
    public ResponseEntity<Map<String, Object>> rejectContent(
            @PathVariable Long contentId,
            HttpServletRequest httpRequest,
            @Valid @RequestBody com.dataelf.platform.dto.ReviewRequest request) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        log.info("Admin {} rejecting content {}", adminId, contentId);
        
        contentService.rejectContent(contentId, adminId, request.getReason());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "内容已拒绝");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/content/{contentId}/publish")
    @Operation(summary = "直接发布内容", description = "管理员直接发布内容，跳过用户确认")
    public ResponseEntity<Map<String, Object>> directPublish(
            @PathVariable Long contentId,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        log.info("Admin {} directly publishing content {}", adminId, contentId);
        
        contentService.directPublish(contentId, adminId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "内容已直接发布");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/content/{contentId}/integrity")
    @Operation(summary = "检查内容完整性", description = "管理员查看内容的结构化完整性评分")
    public ResponseEntity<Map<String, Object>> checkIntegrity(@PathVariable Long contentId) {
        log.info("Admin checking integrity for content {}", contentId);
        
        com.dataelf.platform.dto.IntegrityScore integrityScore = contentService.checkIntegrity(contentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", integrityScore);
        
        return ResponseEntity.ok(response);
    }
}
