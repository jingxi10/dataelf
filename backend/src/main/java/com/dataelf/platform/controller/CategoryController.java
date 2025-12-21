package com.dataelf.platform.controller;

import com.dataelf.platform.dto.CategoryCreateRequest;
import com.dataelf.platform.dto.CategoryDTO;
import com.dataelf.platform.dto.CategoryUpdateRequest;
import com.dataelf.platform.service.CategoryService;
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
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * 创建分类（管理员）
     */
    @PostMapping("/api/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        log.info("Creating category: {}", request.getName());
        
        CategoryDTO category = categoryService.createCategory(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", category);
        response.put("message", "分类创建成功");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 获取所有分类（树形结构）- 公开接口
     */
    @GetMapping("/api/public/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        log.info("Getting all categories");
        
        List<CategoryDTO> categories = categoryService.getAllCategories();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categories);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取分类树（树形结构）- 公开接口
     */
    @GetMapping("/api/public/categories/tree")
    public ResponseEntity<Map<String, Object>> getCategoryTree() {
        log.info("Getting category tree");
        
        List<CategoryDTO> categories = categoryService.getAllCategories();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categories);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取指定分类 - 公开接口
     */
    @GetMapping("/api/public/categories/{id}")
    public ResponseEntity<Map<String, Object>> getCategory(@PathVariable Long id) {
        log.info("Getting category: {}", id);
        
        CategoryDTO category = categoryService.getCategory(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", category);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取子分类 - 公开接口
     */
    @GetMapping("/api/public/categories/{id}/children")
    public ResponseEntity<Map<String, Object>> getChildCategories(@PathVariable Long id) {
        log.info("Getting child categories for: {}", id);
        
        List<CategoryDTO> children = categoryService.getChildCategories(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", children);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新分类（管理员）
     */
    @PutMapping("/api/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        log.info("Updating category: {}", id);
        
        CategoryDTO category = categoryService.updateCategory(id, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", category);
        response.put("message", "分类更新成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除分类（管理员）
     */
    @DeleteMapping("/api/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        log.info("Deleting category: {}", id);
        
        categoryService.deleteCategory(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "分类删除成功");
        
        return ResponseEntity.ok(response);
    }
}
