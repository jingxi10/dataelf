package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateUpdateRequest {
    private String name;
    private String type;
    private String description;
    private String schemaDefinition;
    private String schemaOrgType;
}
