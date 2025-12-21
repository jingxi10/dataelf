package com.dataelf.platform.controller;

import com.dataelf.platform.dto.DataSourceCreateRequest;
import com.dataelf.platform.dto.DataSourceDTO;
import com.dataelf.platform.dto.DataSourceUpdateRequest;
import com.dataelf.platform.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/data-sources")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class DataSourceController {
    
    private final DataSourceService dataSourceService;
    
    /**
     * 创建数据源
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDataSource(@Valid @RequestBody DataSourceCreateRequest request) {
        log.info("Creating data source: {}", request.getName());
        
        DataSourceDTO dataSource = dataSourceService.createDataSource(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dataSource);
        response.put("message", "数据源创建成功");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 获取所有数据源
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDataSources() {
        log.info("Getting all data sources");
        
        List<DataSourceDTO> dataSources = dataSourceService.getAllDataSources();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dataSources);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取启用的数据源
     */
    @GetMapping("/enabled")
    public ResponseEntity<Map<String, Object>> getEnabledDataSources() {
        log.info("Getting enabled data sources");
        
        List<DataSourceDTO> dataSources = dataSourceService.getEnabledDataSources();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dataSources);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取指定数据源
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDataSource(@PathVariable Long id) {
        log.info("Getting data source: {}", id);
        
        DataSourceDTO dataSource = dataSourceService.getDataSource(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dataSource);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新数据源
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDataSource(
            @PathVariable Long id,
            @Valid @RequestBody DataSourceUpdateRequest request) {
        log.info("Updating data source: {}", id);
        
        DataSourceDTO dataSource = dataSourceService.updateDataSource(id, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dataSource);
        response.put("message", "数据源更新成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除数据源
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDataSource(@PathVariable Long id) {
        log.info("Deleting data source: {}", id);
        
        dataSourceService.deleteDataSource(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "数据源删除成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 启用/禁用数据源
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleDataSource(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        log.info("Toggling data source {}: {}", id, enabled);
        
        DataSourceDTO dataSource = dataSourceService.toggleDataSource(id, enabled);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dataSource);
        response.put("message", enabled ? "数据源已启用" : "数据源已禁用");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 手动触发抓取
     */
    @PostMapping("/{id}/fetch")
    public ResponseEntity<Map<String, Object>> triggerFetch(@PathVariable Long id) {
        log.info("Triggering fetch for data source: {}", id);
        
        dataSourceService.triggerFetch(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "抓取任务已触发");
        
        return ResponseEntity.ok(response);
    }
}
