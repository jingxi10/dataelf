-- 添加管理员权限配置字段
ALTER TABLE users ADD COLUMN admin_permissions JSON NULL COMMENT '管理员权限菜单配置，JSON格式数组';
