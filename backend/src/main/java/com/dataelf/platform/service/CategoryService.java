package com.dataelf.platform.service;

import com.dataelf.platform.dto.CategoryCreateRequest;
import com.dataelf.platform.dto.CategoryDTO;
import com.dataelf.platform.dto.CategoryUpdateRequest;
import com.dataelf.platform.entity.Category;
import com.dataelf.platform.exception.ValidationException;
import com.dataelf.platform.repository.CategoryRepository;
import com.dataelf.platform.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final ContentRepository contentRepository;
    
    private static final int MAX_CATEGORY_LEVEL = 3;
    
    /**
     * 创建分类
     */
    @Transactional
    public CategoryDTO createCategory(CategoryCreateRequest request) {
        log.info("Creating category: {}", request.getName());
        
        // 计算层级
        int level = 1;
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new ValidationException("父分类不存在"));
            
            level = parent.getLevel() + 1;
            
            // 验证层级限制（最多3级）
            if (level > MAX_CATEGORY_LEVEL) {
                throw new ValidationException("分类层级不能超过" + MAX_CATEGORY_LEVEL + "级");
            }
        }
        
        Category category = new Category();
        category.setName(request.getName());
        category.setParentId(request.getParentId());
        category.setDescription(request.getDescription());
        category.setLevel(level);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        
        category = categoryRepository.save(category);
        log.info("Category created with id: {}", category.getId());
        
        return convertToDTO(category);
    }
    
    /**
     * 获取所有分类（树形结构）
     */
    public List<CategoryDTO> getAllCategories() {
        log.info("Getting all categories");
        List<Category> allCategories = categoryRepository.findAll();
        
        // 构建树形结构
        return buildCategoryTree(allCategories, null);
    }
    
    /**
     * 获取指定分类
     */
    public CategoryDTO getCategory(Long id) {
        log.info("Getting category: {}", id);
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ValidationException("分类不存在"));
        
        return convertToDTO(category);
    }
    
    /**
     * 获取子分类
     */
    public List<CategoryDTO> getChildCategories(Long parentId) {
        log.info("Getting child categories for parent: {}", parentId);
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(parentId);
        
        return children.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 更新分类
     */
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryUpdateRequest request) {
        log.info("Updating category: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ValidationException("分类不存在"));
        
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        
        category = categoryRepository.save(category);
        log.info("Category updated: {}", id);
        
        return convertToDTO(category);
    }
    
    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ValidationException("分类不存在"));
        
        // 检查是否有子分类
        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new ValidationException("该分类下有子分类，无法删除");
        }
        
        // 检查是否有内容使用该分类
        long contentCount = category.getContents().size();
        if (contentCount > 0) {
            throw new ValidationException("该分类下有" + contentCount + "个内容，无法删除");
        }
        
        categoryRepository.delete(category);
        log.info("Category deleted: {}", id);
    }
    
    /**
     * 构建分类树
     */
    private List<CategoryDTO> buildCategoryTree(List<Category> allCategories, Long parentId) {
        return allCategories.stream()
            .filter(cat -> {
                if (parentId == null) {
                    return cat.getParentId() == null;
                }
                return parentId.equals(cat.getParentId());
            })
            .map(cat -> {
                CategoryDTO dto = convertToDTO(cat);
                dto.setChildren(buildCategoryTree(allCategories, cat.getId()));
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 转换为DTO
     */
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setParentId(category.getParentId());
        dto.setDescription(category.getDescription());
        dto.setLevel(category.getLevel());
        dto.setSortOrder(category.getSortOrder());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setContentCount(category.getContents().size());
        dto.setChildren(new ArrayList<>());
        
        return dto;
    }
}
