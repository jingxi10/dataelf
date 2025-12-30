-- 添加管理员类型字段
ALTER TABLE users ADD COLUMN admin_type VARCHAR(20) NULL;

-- 将现有的ADMIN用户设置为MAIN_ADMIN（主管理员）
UPDATE users SET admin_type = 'MAIN_ADMIN' WHERE role = 'ADMIN' AND admin_type IS NULL;

-- 添加索引
CREATE INDEX idx_admin_type ON users(admin_type);


