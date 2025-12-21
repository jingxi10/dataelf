package com.dataelf.platform.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(nullable = false, length = 500)
    private String url;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceStatus status;
    
    @Column(name = "fetch_interval")
    private Integer fetchInterval; // 抓取间隔（小时）
    
    @Column(name = "last_fetch_time")
    private LocalDateTime lastFetchTime;
    
    @Column(name = "next_fetch_time")
    private LocalDateTime nextFetchTime;
    
    @Column(name = "fetch_count")
    private Integer fetchCount = 0;
    
    @Column(name = "success_count")
    private Integer successCount = 0;
    
    @Column(name = "error_count")
    private Integer errorCount = 0;
    
    @Column(name = "last_error", length = 1000)
    private String lastError;
    
    @Column(name = "selector_config", columnDefinition = "TEXT")
    private String selectorConfig; // JSON格式的选择器配置
    
    @Column(name = "cleaning_rules", columnDefinition = "TEXT")
    private String cleaningRules; // JSON格式的清洗规则
    
    @Column(name = "template_mapping", columnDefinition = "TEXT")
    private String templateMapping; // JSON格式的模板映射
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum SourceType {
        RSS,      // RSS订阅
        HTML,     // HTML网页
        API,      // API接口
        JSON,     // JSON数据
        XML       // XML数据
    }
    
    public enum SourceStatus {
        ACTIVE,   // 活跃
        PAUSED,   // 暂停
        ERROR,    // 错误
        DISABLED  // 禁用
    }
}
