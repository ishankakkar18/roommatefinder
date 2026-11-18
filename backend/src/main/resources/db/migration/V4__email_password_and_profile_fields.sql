-- ============================================================================
-- V4__email_password_and_profile_fields.sql
-- Replaces phone+OTP auth with email + password.
--   * phone becomes optional (kept for future contact verification)
--   * otp_codes is dropped entirely
--   * profiles gains the "personal dashboard" fields
-- ============================================================================

ALTER TABLE users
    ADD COLUMN email         VARCHAR(120) NULL AFTER phone,
    ADD COLUMN password_hash CHAR(60)     NULL AFTER email,
    MODIFY COLUMN phone      VARCHAR(15)  NULL;

ALTER TABLE users
    ADD UNIQUE KEY uk_user_email (email);

DROP TABLE IF EXISTS otp_codes;

ALTER TABLE profiles
    ADD COLUMN education  VARCHAR(80)     NULL AFTER about_me,
    ADD COLUMN age        TINYINT UNSIGNED NULL AFTER education,
    ADD COLUMN birthplace VARCHAR(80)     NULL AFTER age,
    ADD COLUMN occupation VARCHAR(80)     NULL AFTER birthplace;
