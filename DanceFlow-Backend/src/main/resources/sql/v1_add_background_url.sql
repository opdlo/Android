-- ========================================
-- V1: Add background_url to users table
-- ========================================
ALTER TABLE users ADD COLUMN background_url VARCHAR(512) DEFAULT NULL COMMENT 'background image for profile page';
-- Rollback: ALTER TABLE users DROP COLUMN background_url;
