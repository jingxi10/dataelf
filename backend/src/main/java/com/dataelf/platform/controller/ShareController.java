package com.dataelf.platform.controller;

import com.dataelf.platform.dto.ShareLinkResponse;
import com.dataelf.platform.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/share")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "分享", description = "内容分享接口")
public class ShareController {
    
    private final ShareService shareService;
    
    @GetMapping("/{contentId}")
    @Operation(summary = "获取分享链接", description = "获取内容的各平台分享链接（公开）")
    public ResponseEntity<Map<String, Object>> getShareLinks(
            @PathVariable Long contentId
    ) {
        ShareLinkResponse shareLinks = shareService.generateShareLinks(contentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", shareLinks);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{contentId}/record")
    @Operation(summary = "记录分享", description = "记录内容分享行为（公开）")
    public ResponseEntity<Map<String, Object>> recordShare(
            @PathVariable Long contentId,
            @RequestParam String platform
    ) {
        shareService.recordShare(contentId, platform);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "分享记录成功");
        
        return ResponseEntity.ok(response);
    }
}
