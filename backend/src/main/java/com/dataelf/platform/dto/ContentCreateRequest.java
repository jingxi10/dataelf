package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentCreateRequest {
    
    @NotNull(message = "模板ID不能为空")
    private Long templateId;
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    @NotNull(message = "结构化数据不能为空")
    private Map<String, Object> structuredData;
    
    private String copyrightNotice;
    
    private String contentSource;
    
    private String authorName;
    
    private Boolean isOriginal = true;
    
    private List<Long> categoryIds;
    
    private List<String> tagNames;
    
    private List<String> fieldOrder;
}
