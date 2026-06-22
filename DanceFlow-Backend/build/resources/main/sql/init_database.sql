-- ========================================
-- 智舞流光 (DanceFlow) 数据库初始化脚本
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS danceflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE danceflow;

-- ========================================
-- 用户表 (users)
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    signature VARCHAR(255),
    avatar_url VARCHAR(512),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 帖子表 (posts)
-- ========================================
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    images JSON,
    video_url VARCHAR(512),
    likes_count INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_author_id (author_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 评论表 (comments)
-- ========================================
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 点赞记录表 (like_records)
-- ========================================
CREATE TABLE IF NOT EXISTS like_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_post (user_id, post_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 视频分类表 (video_categories)
-- ========================================
CREATE TABLE IF NOT EXISTS video_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL UNIQUE,
    description TEXT,
    cover_url VARCHAR(512),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 视频资源表 (video_resources)
-- ========================================
CREATE TABLE IF NOT EXISTS video_resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover_url VARCHAR(512),
    video_url VARCHAR(512) NOT NULL,
    category_id BIGINT,
    duration INT,
    views INT DEFAULT 0,
    likes_count INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES video_categories(id) ON DELETE SET NULL,
    INDEX idx_category_id (category_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 练习历史表 (practice_history)
-- ========================================
CREATE TABLE IF NOT EXISTS practice_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    video_url VARCHAR(512) NOT NULL,
    score FLOAT,
    analysis_status VARCHAR(20) DEFAULT 'pending',
    feedback TEXT,
    suggestions JSON,
    dance_style VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (analysis_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 收藏表 (favorites)
-- ========================================
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    post_id BIGINT,
    video_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES video_resources(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_post_id (post_id),
    INDEX idx_video_id (video_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 插入示例数据
-- ========================================

-- 插入视频分类
INSERT INTO video_categories (name, description) VALUES
('街舞', 'Hip Hop、Breaking 等街头舞蹈'),
('爵士舞', 'Jazz 舞蹈教学'),
('现代舞', '现代舞、当代舞'),
('拉丁舞', '拉丁舞蹈教学'),
('芭蕾', '芭蕾基础教学'),
('中国舞', '中国传统舞蹈'),
('韩舞', 'K-Pop 舞蹈教学'),
('编舞', '原创编舞教学')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 插入示例视频
INSERT INTO video_resources (title, description, video_url, category_id, duration, cover_url) VALUES
('街舞基础教学 - 第一节', '适合初学者的街舞入门课程', 'https://example.com/video1.mp4', 1, 600, 'https://example.com/cover1.jpg'),
('爵士舞完整套路', '完整的爵士舞组合教学', 'https://example.com/video2.mp4', 2, 480, 'https://example.com/cover2.jpg'),
('现代舞基础训练', '现代舞基础动作训练', 'https://example.com/video3.mp4', 3, 720, 'https://example.com/cover3.jpg')
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- ========================================
-- 完成
-- ========================================
SELECT 'Database initialization completed successfully!' AS message;
