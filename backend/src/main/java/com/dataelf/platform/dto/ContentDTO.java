package com.dataelf.platform.dto;

import com.dataelf.platform.entity.Content;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    
    private Long id;
    private Long userId;
    private Long templateId;
    private String title;
    private Map<String, Object> structuredData;
    private String jsonLd;
    private String htmlOutput;
    private String markdownOutput;
    private String copyrightNotice;
    private String contentSource;
    private String authorName;
    private Boolean isOriginal;
    private Content.ContentStatus status;
    private BigDecimal integrityScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime publishedAt;
    private Long reviewedBy;
    private String rejectReason;
    private Integer viewCount;
    private List<Long> categoryIds;
    private List<String> tagNames;
}
