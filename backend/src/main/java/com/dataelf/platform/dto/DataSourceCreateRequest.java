package com.dataelf.platform.dto;

import com.dataelf.platform.entity.DataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceCreateRequest {
    
    @NotBlank(message = "数据源名称不能为空")
    private String name;
    
    @NotBlank(message = "数据源URL不能为空")
    private String url;
    
    private String description;
    
    @NotNull(message = "数据源类型不能为空")
    private DataSource.SourceType sourceType;
    
    @NotNull(message = "抓取间隔不能为空")
    @Min(value = 1, message = "抓取间隔至少为1小时")
    private Integer fetchInterval;
    
    private String selectorConfig;
    private String cleaningRules;
    private String templateMapping;
}
