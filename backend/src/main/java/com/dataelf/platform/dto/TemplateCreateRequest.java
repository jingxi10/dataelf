package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreateRequest {
    
    @NotBlank(message = "模板名称不能为空")
    private String name;
    
    @NotBlank(message = "模板类型不能为空")
    private String type;
    
    private String description;
    
    @NotBlank(message = "Schema定义不能为空")
    private String schemaDefinition;
    
    @NotBlank(message = "Schema.org类型不能为空")
    private String schemaOrgType;
}
