-- 添加评论管理字段
ALTER TABLE comments ADD COLUMN is_pinned BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE comments ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE NOT NULL;

-- 添加索引
CREATE INDEX idx_is_pinned ON comments(is_pinned);
CREATE INDEX idx_is_deleted ON comments(is_deleted);


