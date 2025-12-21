package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    
    private Long id;
    private String name;
    private Long parentId;
    private String description;
    private Integer level;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private List<CategoryDTO> children;
    private Integer contentCount;
}
