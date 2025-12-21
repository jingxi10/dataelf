package com.dataelf.platform.dto;

import com.dataelf.platform.entity.DataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceDTO {
    
    private Long id;
    private String name;
    private String url;
    private String description;
    private DataSource.SourceType sourceType;
    private DataSource.SourceStatus status;
    private Integer fetchInterval;
    private LocalDateTime lastFetchTime;
    private LocalDateTime nextFetchTime;
    private Integer fetchCount;
    private Integer successCount;
    private Integer errorCount;
    private String lastError;
    private String selectorConfig;
    private String cleaningRules;
    private String templateMapping;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
