package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigUpdateRequest {
    
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    
    private String description;
}
