-- =====================================================
-- 数流精灵 - 创建缺失的数据库表
-- 运行方式: mysql -u root -p your_database < create_missing_tables.sql
-- =====================================================

-- 创建数据源表
CREATE TABLE IF NOT EXISTS data_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    url VARCHAR(500) NOT NULL,
    description VARCHAR(1000),
    source_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    fetch_interval INT,
    last_fetch_time DATETIME,
    next_fetch_time DATETIME,
    fetch_count INT DEFAULT 0,
    success_count INT DEFAULT 0,
    error_count INT DEFAULT 0,
    last_error VARCHAR(1000),
    selector_config TEXT,
    cleaning_rules TEXT,
    template_mapping TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_data_sources_status (status),
    INDEX idx_data_sources_source_type (source_type),
    INDEX idx_data_sources_enabled (enabled),
    INDEX idx_data_sources_next_fetch (next_fetch_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建评论表
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content_id BIGINT NOT NULL,
    comment_text TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comments_content_id (content_id),
    INDEX idx_comments_user_id (user_id),
    INDEX idx_comments_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description TEXT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_system_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入默认系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('site_name', '数流精灵', '网站名称'),
('site_description', '智能数据管理平台', '网站描述'),
('site_keywords', '数据管理,AI,结构化数据', '网站关键词'),
('contact_email', 'admin@dataelf.com', '联系邮箱'),
('page_size', '20', '默认分页大小')
ON DUPLICATE KEY UPDATE config_key = config_key;

-- 验证表创建成功
SELECT 'Tables created successfully!' AS result;
SHOW TABLES LIKE 'data_sources';
SHOW TABLES LIKE 'comments';
SHOW TABLES LIKE 'system_config';
