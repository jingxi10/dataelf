package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeDTO {
    
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private Integer contentCount;
    
    // 子分类列表
    private List<CategoryTreeDTO> children = new ArrayList<>();
    
    // 是否有子分类
    private Boolean hasChildren;
}
