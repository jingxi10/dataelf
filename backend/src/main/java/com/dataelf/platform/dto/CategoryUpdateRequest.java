package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {
    
    @Size(max = 255, message = "分类名称不能超过255个字符")
    private String name;
    
    @Size(max = 1000, message = "描述不能超过1000个字符")
    private String description;
    
    private Integer sortOrder;
}
