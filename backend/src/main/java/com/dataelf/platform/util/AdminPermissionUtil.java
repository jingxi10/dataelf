package com.dataelf.platform.util;

import com.dataelf.platform.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员权限工具类
 */
@Slf4j
public class AdminPermissionUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 权限菜单定义
    public static final String PERMISSION_USER_APPROVE = "user_approve"; // 审核会员（对应延长时长按钮）
    public static final String PERMISSION_USER_DELETE = "user_delete"; // 删减会员（对应删除会员按钮）
    public static final String PERMISSION_CONTENT_REVIEW = "content_review"; // 审核内容（对应内容审核菜单）
    public static final String PERMISSION_CONTENT_DELETE = "content_delete"; // 删减内容
    public static final String PERMISSION_CONTENT_VIEW_OWN = "content_view_own"; // 查看自己审核的内容
    public static final String PERMISSION_CONTENT_UNPUBLISH_OWN = "content_unpublish_own"; // 下架或删除自己审核的内容
    
    // 菜单权限定义
    public static final String MENU_TEMPLATE_MANAGE = "template_manage"; // 模板管理
    public static final String MENU_TAG_MANAGE = "tag_manage"; // 标签管理
    public static final String MENU_CATEGORY_MANAGE = "category_manage"; // 分类管理
    public static final String MENU_DATA_SOURCE_MANAGE = "data_source_manage"; // 数据源管理
    public static final String MENU_SYSTEM_SETTINGS = "system_settings"; // 系统设置
    
    /**
     * 检查用户是否有指定权限
     * @param user 用户对象
     * @param permission 权限代码
     * @return 是否有权限
     */
    public static boolean hasPermission(User user, String permission) {
        if (user == null) {
            return false;
        }
        
        // 主管理员拥有所有权限
        if (isMainAdmin(user)) {
            return true;
        }
        
        // 普通用户没有管理员权限
        if (user.getRole() != User.UserRole.ADMIN) {
            return false;
        }
        
        // 普通管理员需要检查权限配置
        if (user.getAdminType() == User.AdminType.NORMAL_ADMIN) {
            List<String> permissions = getPermissions(user);
            return permissions.contains(permission);
        }
        
        return false;
    }
    
    /**
     * 获取用户的所有权限列表
     * @param user 用户对象
     * @return 权限列表
     */
    public static List<String> getPermissions(User user) {
        if (user.getAdminType() == User.AdminType.MAIN_ADMIN) {
            // 主管理员拥有所有权限
            List<String> allPermissions = new ArrayList<>();
            allPermissions.add(PERMISSION_USER_APPROVE);
            allPermissions.add(PERMISSION_USER_DELETE);
            allPermissions.add(PERMISSION_CONTENT_REVIEW);
            allPermissions.add(PERMISSION_CONTENT_DELETE);
            allPermissions.add(PERMISSION_CONTENT_VIEW_OWN);
            allPermissions.add(PERMISSION_CONTENT_UNPUBLISH_OWN);
            allPermissions.add(MENU_TEMPLATE_MANAGE);
            allPermissions.add(MENU_TAG_MANAGE);
            allPermissions.add(MENU_CATEGORY_MANAGE);
            allPermissions.add(MENU_DATA_SOURCE_MANAGE);
            allPermissions.add(MENU_SYSTEM_SETTINGS);
            return allPermissions;
        }
        
        if (user.getAdminType() == User.AdminType.NORMAL_ADMIN && user.getAdminPermissions() != null) {
            try {
                return objectMapper.readValue(user.getAdminPermissions(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.error("Failed to parse admin permissions: {}", e.getMessage());
                return new ArrayList<>();
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 检查是否是主管理员
     * @param user 用户对象
     * @return 是否是主管理员
     */
    public static boolean isMainAdmin(User user) {
        if (user == null) {
            return false;
        }
        
        // 必须是管理员角色
        if (user.getRole() != User.UserRole.ADMIN) {
            return false;
        }
        
        // adminType是MAIN_ADMIN，或者是null（兼容旧数据，旧的主管理员可能adminType为null）
        return user.getAdminType() == User.AdminType.MAIN_ADMIN || 
               user.getAdminType() == null;
    }
    
    /**
     * 检查是否是普通管理员
     * @param user 用户对象
     * @return 是否是普通管理员
     */
    public static boolean isNormalAdmin(User user) {
        return user.getRole() == User.UserRole.ADMIN && 
               user.getAdminType() == User.AdminType.NORMAL_ADMIN;
    }
}
