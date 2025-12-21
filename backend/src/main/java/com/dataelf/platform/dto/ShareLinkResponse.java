package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareLinkResponse {
    
    private String contentUrl;
    private String title;
    private String description;
    private Map<String, String> shareLinks;
    
    // 保留旧字段以兼容
    private String shareUrl;
    private String htmlLink;
    private Long contentId;
}
