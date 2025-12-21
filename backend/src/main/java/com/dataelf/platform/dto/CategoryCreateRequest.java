package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 255, message = "分类名称不能超过255个字符")
    private String name;
    
    private Long parentId;
    
    @Size(max = 1000, message = "描述不能超过1000个字符")
    private String description;
    
    private Integer sortOrder;
}
