package com.dataelf.platform.dto;

import com.dataelf.platform.entity.DataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceUpdateRequest {
    
    private String name;
    private String url;
    private String description;
    private DataSource.SourceType sourceType;
    
    @Min(value = 1, message = "抓取间隔至少为1小时")
    private Integer fetchInterval;
    
    private String selectorConfig;
    private String cleaningRules;
    private String templateMapping;
    private Boolean enabled;
}
