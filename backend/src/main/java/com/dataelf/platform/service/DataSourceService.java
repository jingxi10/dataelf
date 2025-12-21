package com.dataelf.platform.service;

import com.dataelf.platform.dto.DataSourceCreateRequest;
import com.dataelf.platform.dto.DataSourceDTO;
import com.dataelf.platform.dto.DataSourceUpdateRequest;
import com.dataelf.platform.entity.DataSource;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.DataSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSourceService {
    
    private final DataSourceRepository dataSourceRepository;
    
    /**
     * 创建数据源
     */
    @Transactional
    public DataSourceDTO createDataSource(DataSourceCreateRequest request) {
        log.info("Creating data source: {}", request.getName());
        
        DataSource dataSource = new DataSource();
        dataSource.setName(request.getName());
        dataSource.setUrl(request.getUrl());
        dataSource.setDescription(request.getDescription());
        dataSource.setSourceType(request.getSourceType());
        dataSource.setStatus(DataSource.SourceStatus.ACTIVE);
        dataSource.setFetchInterval(request.getFetchInterval());
        dataSource.setSelectorConfig(request.getSelectorConfig());
        dataSource.setCleaningRules(request.getCleaningRules());
        dataSource.setTemplateMapping(request.getTemplateMapping());
        dataSource.setEnabled(true);
        dataSource.setFetchCount(0);
        dataSource.setSuccessCount(0);
        dataSource.setErrorCount(0);
        
        // 计算下次抓取时间
        dataSource.setNextFetchTime(LocalDateTime.now().plusHours(request.getFetchInterval()));
        
        dataSource = dataSourceRepository.save(dataSource);
        log.info("Data source created with id: {}", dataSource.getId());
        
        return convertToDTO(dataSource);
    }
    
    /**
     * 获取所有数据源
     */
    public List<DataSourceDTO> getAllDataSources() {
        log.info("Getting all data sources");
        List<DataSource> dataSources = dataSourceRepository.findAll();
        
        return dataSources.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取启用的数据源
     */
    public List<DataSourceDTO> getEnabledDataSources() {
        log.info("Getting enabled data sources");
        List<DataSource> dataSources = dataSourceRepository.findByEnabledTrue();
        
        return dataSources.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取指定数据源
     */
    public DataSourceDTO getDataSource(Long id) {
        log.info("Getting data source: {}", id);
        DataSource dataSource = dataSourceRepository.findById(id)
            .orElseThrow(() -> new ValidationException("数据源不存在"));
        
        return convertToDTO(dataSource);
    }
    
    /**
     * 更新数据源
     */
    @Transactional
    public DataSourceDTO updateDataSource(Long id, DataSourceUpdateRequest request) {
        log.info("Updating data source: {}", id);
        
        DataSource dataSource = dataSourceRepository.findById(id)
            .orElseThrow(() -> new ValidationException("数据源不存在"));
        
        if (request.getName() != null) {
            dataSource.setName(request.getName());
        }
        if (request.getUrl() != null) {
            dataSource.setUrl(request.getUrl());
        }
        if (request.getDescription() != null) {
            dataSource.setDescription(request.getDescription());
        }
        if (request.getSourceType() != null) {
            dataSource.setSourceType(request.getSourceType());
        }
        if (request.getFetchInterval() != null) {
            dataSource.setFetchInterval(request.getFetchInterval());
            // 重新计算下次抓取时间
            dataSource.setNextFetchTime(LocalDateTime.now().plusHours(request.getFetchInterval()));
        }
        if (request.getSelectorConfig() != null) {
            dataSource.setSelectorConfig(request.getSelectorConfig());
        }
        if (request.getCleaningRules() != null) {
            dataSource.setCleaningRules(request.getCleaningRules());
        }
        if (request.getTemplateMapping() != null) {
            dataSource.setTemplateMapping(request.getTemplateMapping());
        }
        if (request.getEnabled() != null) {
            dataSource.setEnabled(request.getEnabled());
            if (!request.getEnabled()) {
                dataSource.setStatus(DataSource.SourceStatus.DISABLED);
            } else if (dataSource.getStatus() == DataSource.SourceStatus.DISABLED) {
                dataSource.setStatus(DataSource.SourceStatus.ACTIVE);
            }
        }
        
        dataSource = dataSourceRepository.save(dataSource);
        log.info("Data source updated: {}", id);
        
        return convertToDTO(dataSource);
    }
    
    /**
     * 删除数据源
     */
    @Transactional
    public void deleteDataSource(Long id) {
        log.info("Deleting data source: {}", id);
        
        DataSource dataSource = dataSourceRepository.findById(id)
            .orElseThrow(() -> new ValidationException("数据源不存在"));
        
        dataSourceRepository.delete(dataSource);
        log.info("Data source deleted: {}", id);
    }
    
    /**
     * 启用/禁用数据源
     */
    @Transactional
    public DataSourceDTO toggleDataSource(Long id, boolean enabled) {
        log.info("Toggling data source {}: {}", id, enabled);
        
        DataSource dataSource = dataSourceRepository.findById(id)
            .orElseThrow(() -> new ValidationException("数据源不存在"));
        
        dataSource.setEnabled(enabled);
        if (enabled) {
            if (dataSource.getStatus() == DataSource.SourceStatus.DISABLED) {
                dataSource.setStatus(DataSource.SourceStatus.ACTIVE);
            }
        } else {
            dataSource.setStatus(DataSource.SourceStatus.DISABLED);
        }
        
        dataSource = dataSourceRepository.save(dataSource);
        return convertToDTO(dataSource);
    }
    
    /**
     * 手动触发抓取
     */
    @Transactional
    public void triggerFetch(Long id) {
        log.info("Triggering fetch for data source: {}", id);
        
        DataSource dataSource = dataSourceRepository.findById(id)
            .orElseThrow(() -> new ValidationException("数据源不存在"));
        
        if (!dataSource.getEnabled()) {
            throw new ValidationException("数据源已禁用");
        }
        
        // 这里应该调用实际的抓取服务
        // 简化实现：只更新时间和计数
        dataSource.setLastFetchTime(LocalDateTime.now());
        dataSource.setNextFetchTime(LocalDateTime.now().plusHours(dataSource.getFetchInterval()));
        dataSource.setFetchCount(dataSource.getFetchCount() + 1);
        
        dataSourceRepository.save(dataSource);
        log.info("Fetch triggered for data source: {}", id);
    }
    
    /**
     * 获取需要抓取的数据源
     */
    public List<DataSource> getDataSourcesNeedingFetch() {
        return dataSourceRepository.findDataSourcesNeedingFetch(LocalDateTime.now());
    }
    
    /**
     * 更新抓取结果
     */
    @Transactional
    public void updateFetchResult(Long id, boolean success, String error) {
        DataSource dataSource = dataSourceRepository.findById(id)
            .orElseThrow(() -> new ValidationException("数据源不存在"));
        
        dataSource.setLastFetchTime(LocalDateTime.now());
        dataSource.setNextFetchTime(LocalDateTime.now().plusHours(dataSource.getFetchInterval()));
        dataSource.setFetchCount(dataSource.getFetchCount() + 1);
        
        if (success) {
            dataSource.setSuccessCount(dataSource.getSuccessCount() + 1);
            dataSource.setStatus(DataSource.SourceStatus.ACTIVE);
            dataSource.setLastError(null);
        } else {
            dataSource.setErrorCount(dataSource.getErrorCount() + 1);
            dataSource.setStatus(DataSource.SourceStatus.ERROR);
            dataSource.setLastError(error);
        }
        
        dataSourceRepository.save(dataSource);
    }
    
    /**
     * 转换为DTO
     */
    private DataSourceDTO convertToDTO(DataSource dataSource) {
        DataSourceDTO dto = new DataSourceDTO();
        dto.setId(dataSource.getId());
        dto.setName(dataSource.getName());
        dto.setUrl(dataSource.getUrl());
        dto.setDescription(dataSource.getDescription());
        dto.setSourceType(dataSource.getSourceType());
        dto.setStatus(dataSource.getStatus());
        dto.setFetchInterval(dataSource.getFetchInterval());
        dto.setLastFetchTime(dataSource.getLastFetchTime());
        dto.setNextFetchTime(dataSource.getNextFetchTime());
        dto.setFetchCount(dataSource.getFetchCount());
        dto.setSuccessCount(dataSource.getSuccessCount());
        dto.setErrorCount(dataSource.getErrorCount());
        dto.setLastError(dataSource.getLastError());
        dto.setSelectorConfig(dataSource.getSelectorConfig());
        dto.setCleaningRules(dataSource.getCleaningRules());
        dto.setTemplateMapping(dataSource.getTemplateMapping());
        dto.setEnabled(dataSource.getEnabled());
        dto.setCreatedAt(dataSource.getCreatedAt());
        dto.setUpdatedAt(dataSource.getUpdatedAt());
        
        return dto;
    }
}
