package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentUpdateRequest {
    
    private String title;
    
    private Map<String, Object> structuredData;
    
    private String copyrightNotice;
    
    private String contentSource;
    
    private String authorName;
    
    private Boolean isOriginal;
    
    private List<Long> categoryIds;
    
    private List<String> tagNames;
    
    private List<String> fieldOrder;
}
