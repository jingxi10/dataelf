package com.dataelf.platform.controller;

import com.dataelf.platform.dto.ApproveUserRequest;
import com.dataelf.platform.dto.CreateAdminRequest;
import com.dataelf.platform.dto.ExtendAccountRequest;
import com.dataelf.platform.dto.UpdateAdminPermissionsRequest;
import com.dataelf.platform.dto.UserDTO;
import com.dataelf.platform.entity.User;
import com.dataelf.platform.repository.UserRepository;
import com.dataelf.platform.service.UserService;
import com.dataelf.platform.util.JwtUtil;
import com.dataelf.platform.util.AdminPermissionUtil;
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
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final ObjectMapper objectMapper;
    
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
    
    /**
     * 检查当前管理员是否是主管理员
     */
    private boolean isMainAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        return AdminPermissionUtil.isMainAdmin(admin);
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
        
        // 检查是否是主管理员
        if (!isMainAdmin(adminId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只有主管理员可以创建管理员账号");
            return ResponseEntity.status(403).body(response);
        }
        
        // 创建管理员用户
        User admin = new User();
        admin.setEmail(request.getEmail());
        admin.setPhone(request.getPhone());
        admin.setPasswordHash(passwordEncoder().encode(request.getPassword()));
        admin.setRole(User.UserRole.ADMIN);
        admin.setAdminType(request.getAdminType() != null ? request.getAdminType() : User.AdminType.NORMAL_ADMIN);
        admin.setStatus(User.UserStatus.APPROVED);
        admin.setApprovedAt(LocalDateTime.now());
        admin.setApprovedBy(adminId);
        admin.setExpiresAt(LocalDateTime.now().plusDays(31)); // 默认31天有效期
        
        // 设置权限配置
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            try {
                admin.setAdminPermissions(objectMapper.writeValueAsString(request.getPermissions()));
            } catch (Exception e) {
                log.error("Failed to serialize permissions: {}", e.getMessage());
            }
        }
        
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
                    主管理员拥有所有权限，普通管理员需要user_approve权限。
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
                    description = "无权限"
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
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 检查权限：主管理员拥有所有权限，普通管理员需要user_approve权限
        boolean isMainAdmin = AdminPermissionUtil.isMainAdmin(admin);
        boolean hasPermission = isMainAdmin || AdminPermissionUtil.hasPermission(admin, AdminPermissionUtil.PERMISSION_USER_APPROVE);
        
        if (!hasPermission) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "无权限操作：需要审核会员权限");
            return ResponseEntity.status(403).body(response);
        }
        
        log.info("Admin {} approving user {}", adminId, request.getUserId());
        
        userService.approveUser(request.getUserId(), adminId, request.getValidDays());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "用户账号已批准");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/users/extend")
    @Operation(summary = "延长账号时长", description = "管理员延长用户账号的有效期，主管理员拥有所有权限，普通管理员需要user_approve权限")
    public ResponseEntity<Map<String, Object>> extendAccount(
            @Valid @RequestBody ExtendAccountRequest request,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 检查权限：主管理员拥有所有权限，普通管理员需要user_approve权限
        boolean isMainAdmin = AdminPermissionUtil.isMainAdmin(admin);
        boolean hasPermission = isMainAdmin || AdminPermissionUtil.hasPermission(admin, AdminPermissionUtil.PERMISSION_USER_APPROVE);
        
        if (!hasPermission) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "无权限操作：需要审核会员权限");
            return ResponseEntity.status(403).body(response);
        }
        
        log.info("Admin {} extending account for user {} by {} days", 
                adminId, request.getUserId(), request.getDays());
        
        userService.extendAccount(request.getUserId(), request.getDays());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "账号时长已延长");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/users/{userId}/reduce-days")
    @Operation(summary = "减少账号时长", description = "管理员减少用户账号的有效期")
    public ResponseEntity<Map<String, Object>> reduceAccountDays(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> request,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        Integer days = request != null ? request.get("days") : null;
        
        if (days == null || days <= 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "天数必须大于0");
            return ResponseEntity.badRequest().body(response);
        }
        
        log.info("Admin {} reducing account days for user {} by {} days", adminId, userId, days);
        
        userService.reduceAccountDays(userId, days);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "账号时长已减少");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/users/{userId}/disable")
    @Operation(summary = "停用账号", description = "管理员停用用户账号（设置为过期状态）")
    public ResponseEntity<Map<String, Object>> disableAccount(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        log.info("Admin {} disabling account for user {}", adminId, userId);
        
        userService.disableAccount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "账号已停用");
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "删除会员", description = "管理员删除会员账号，需要user_delete权限或主管理员权限（主管理员可以删除普通管理员）")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 检查权限：主管理员拥有所有权限，普通管理员需要user_delete权限
        boolean isMainAdmin = AdminPermissionUtil.isMainAdmin(admin);
        boolean hasPermission = isMainAdmin || AdminPermissionUtil.hasPermission(admin, AdminPermissionUtil.PERMISSION_USER_DELETE);
        
        if (!hasPermission) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "无权限操作：需要删减会员权限");
            return ResponseEntity.status(403).body(response);
        }
        
        log.info("Admin {} deleting user {}", adminId, userId);
        
        userService.deleteUser(userId, adminId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "会员已删除");
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/users/{userId}/permissions")
    @Operation(summary = "更新管理员权限", description = "主管理员更新普通管理员的权限配置")
    public ResponseEntity<Map<String, Object>> updateAdminPermissions(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateAdminPermissionsRequest request,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 只有主管理员可以更新权限
        if (!isMainAdmin(adminId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只有主管理员可以更新管理员权限");
            return ResponseEntity.status(403).body(response);
        }
        
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 只能更新普通管理员的权限
        if (targetUser.getRole() != User.UserRole.ADMIN || 
            targetUser.getAdminType() != User.AdminType.NORMAL_ADMIN) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只能更新普通管理员的权限");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 更新权限配置
        try {
            targetUser.setAdminPermissions(objectMapper.writeValueAsString(request.getPermissions()));
            userRepository.save(targetUser);
            
            log.info("Admin {} updated permissions for admin user {}", adminId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "权限更新成功");
            response.put("data", UserDTO.fromEntity(targetUser));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update admin permissions: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/users/{userId}")
    @Operation(summary = "查看用户详情", description = "管理员查看用户账号详细信息")
    public ResponseEntity<Map<String, Object>> getUserDetails(@PathVariable Long userId) {
        log.info("Admin fetching details for user {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        UserDTO userDTO = UserDTO.fromEntity(user);
        
        // 填充批准人信息
        if (user.getApprovedBy() != null) {
            User approver = userRepository.findById(user.getApprovedBy()).orElse(null);
            if (approver != null) {
                userDTO.setApprovedByName(approver.getEmail());
            }
        }
        
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
        
        // 收集所有需要查询的批准人ID
        List<Long> approverIds = userPage.getContent().stream()
                .map(User::getApprovedBy)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询批准人信息
        final Map<Long, User> approverMap;
        if (!approverIds.isEmpty()) {
            approverMap = userRepository.findAllById(approverIds).stream()
                    .collect(Collectors.toMap(User::getId, approver -> approver));
        } else {
            approverMap = new HashMap<>();
        }
        
        // 转换为DTO并填充批准人信息
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(user -> {
                    UserDTO dto = UserDTO.fromEntity(user);
                    if (user.getApprovedBy() != null) {
                        User approver = approverMap.get(user.getApprovedBy());
                        if (approver != null) {
                            dto.setApprovedByName(approver.getEmail());
                        }
                    }
                    return dto;
                })
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
    @Operation(summary = "获取审核队列", description = "管理员查看待审核的内容列表（所有管理员都可以查看所有待审核内容）")
    public ResponseEntity<Map<String, Object>> getReviewQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin fetching review queue, page: {}, size: {}", page, size);
        
        // 所有管理员都可以查看待审核队列
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
    
    @DeleteMapping("/content/{contentId}")
    @Operation(summary = "管理员删除内容", description = "主管理员可以删除任何状态的内容（包括已发布的内容）")
    public ResponseEntity<Map<String, Object>> adminDeleteContent(
            @PathVariable Long contentId,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 只有主管理员可以删除任何内容
        if (!isMainAdmin(adminId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只有主管理员可以删除内容");
            return ResponseEntity.status(403).body(response);
        }
        
        log.info("Main admin {} deleting content {}", adminId, contentId);
        
        contentService.adminDeleteContent(contentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "内容已删除");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/content/{contentId}/unpublish")
    @Operation(summary = "管理员下架内容", description = "主管理员可以下架任何状态的内容（改为已拒绝状态）")
    public ResponseEntity<Map<String, Object>> adminUnpublishContent(
            @PathVariable Long contentId,
            @RequestBody(required = false) Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 只有主管理员可以下架任何内容
        if (!isMainAdmin(adminId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只有主管理员可以下架内容");
            return ResponseEntity.status(403).body(response);
        }
        
        String reason = request != null ? request.get("reason") : null;
        log.info("Admin {} unpublishing content {}", adminId, contentId);
        
        contentService.adminUnpublishContent(contentId, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "内容已下架");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/content/all")
    @Operation(summary = "获取所有内容", description = "主管理员查看平台所有内容（包括所有状态）")
    public ResponseEntity<Map<String, Object>> getAllContents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        
        // 只有主管理员可以查看所有内容
        if (!isMainAdmin(adminId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只有主管理员可以查看所有内容");
            return ResponseEntity.status(403).body(response);
        }
        
        log.info("Main admin {} fetching all contents, page: {}, size: {}, status: {}, search: {}", 
                adminId, page, size, status, search);
        
        org.springframework.data.domain.Page<com.dataelf.platform.dto.ContentDTO> contents = 
            contentService.getAllContents(status, search, org.springframework.data.domain.PageRequest.of(page, size));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", contents.getContent());
        response.put("totalPages", contents.getTotalPages());
        response.put("totalElements", contents.getTotalElements());
        response.put("currentPage", contents.getNumber());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/content/review-statistics")
    @Operation(summary = "获取审核统计", description = "获取当前管理员的审核统计信息（通过、拒绝、发布数量）")
    public ResponseEntity<Map<String, Object>> getReviewStatistics(HttpServletRequest httpRequest) {
        Long adminId = getAdminIdFromRequest(httpRequest);
        log.info("Admin {} fetching review statistics", adminId);
        
        Map<String, Object> stats = contentService.getReviewStatistics(adminId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/content/review-history")
    @Operation(summary = "获取审核记录", description = "获取当前管理员的审核记录列表（普通管理员只能查看自己审核的内容）")
    public ResponseEntity<Map<String, Object>> getReviewHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        log.info("Admin {} fetching review history, page: {}, size: {}", adminId, page, size);
        
        // 普通管理员只能查看自己审核的内容
        org.springframework.data.domain.Page<com.dataelf.platform.dto.ContentDTO> history = 
            contentService.getReviewHistory(adminId, 
                org.springframework.data.domain.PageRequest.of(page, size));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", history.getContent());
        response.put("totalPages", history.getTotalPages());
        response.put("totalElements", history.getTotalElements());
        response.put("currentPage", history.getNumber());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/content/all-review-history")
    @Operation(summary = "获取所有审核记录", description = "主管理员查看所有审核记录（包括所有管理员审核的内容）")
    public ResponseEntity<Map<String, Object>> getAllReviewHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        
        Long adminId = getAdminIdFromRequest(httpRequest);
        log.info("Admin {} fetching all review history, page: {}, size: {}", adminId, page, size);
        
        // 检查是否是主管理员
        if (!isMainAdmin(adminId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只有主管理员可以查看所有审核记录");
            return ResponseEntity.status(403).body(response);
        }
        
        org.springframework.data.domain.Page<com.dataelf.platform.dto.ContentDTO> history = 
            contentService.getAllReviewHistory(
                org.springframework.data.domain.PageRequest.of(page, size));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", history.getContent());
        response.put("totalPages", history.getTotalPages());
        response.put("totalElements", history.getTotalElements());
        response.put("currentPage", history.getNumber());
        
        return ResponseEntity.ok(response);
    }
}
