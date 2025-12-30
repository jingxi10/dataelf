-- 添加昵称字段到 users 表
ALTER TABLE users ADD COLUMN nickname VARCHAR(50) DEFAULT NULL AFTER password_hash;
