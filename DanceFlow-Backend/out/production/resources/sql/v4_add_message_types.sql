-- v4_add_message_support.sql
-- Add support for different message types (text, image, video, post_share)

ALTER TABLE messages
    ADD COLUMN message_type VARCHAR(20) DEFAULT 'text',
    ADD COLUMN media_url TEXT,
    ADD COLUMN reference_type VARCHAR(20),
    ADD COLUMN reference_id BIGINT;
