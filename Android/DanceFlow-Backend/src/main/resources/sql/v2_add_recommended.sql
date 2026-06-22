-- ========================================
-- V2: Add recommended flag to video_resources
-- ========================================
ALTER TABLE video_resources ADD COLUMN recommended BOOLEAN DEFAULT FALSE COMMENT '是否推荐';
UPDATE video_resources SET recommended = TRUE WHERE id <= 3;
