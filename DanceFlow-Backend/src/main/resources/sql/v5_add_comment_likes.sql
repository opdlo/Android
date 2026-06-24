-- Add likes_count to comments for sorting by popularity
ALTER TABLE comments ADD COLUMN likes_count INT DEFAULT 0;
