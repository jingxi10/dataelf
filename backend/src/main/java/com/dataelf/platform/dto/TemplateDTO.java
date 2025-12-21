package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDTO {
    private Long id;
    private String name;
    private String type;
    private String description;
    private String schemaDefinition;
    private String schemaOrgType;
    private Boolean isSystem;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
