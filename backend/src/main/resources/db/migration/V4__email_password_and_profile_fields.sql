-- ============================================================================
-- V4__email_password_and_profile_fields.sql
-- Replaces phone+OTP auth with email + password.
--   * phone becomes optional (kept for future contact verification)
--   * otp_codes is dropped entirely
--   * profiles gains the "personal dashboard" fields
--
-- Statements are split deliberately: referencing a column added earlier in the
-- SAME ALTER (e.g. `AFTER email`) works on MySQL but not on TiDB.
-- ============================================================================

ALTER TABLE users ADD COLUMN email VARCHAR(120) NULL AFTER phone;
ALTER TABLE users ADD COLUMN password_hash CHAR(60) NULL AFTER email;
ALTER TABLE users MODIFY COLUMN phone VARCHAR(15) NULL;
ALTER TABLE users ADD UNIQUE KEY uk_user_email (email);

DROP TABLE IF EXISTS otp_codes;

ALTER TABLE profiles ADD COLUMN education VARCHAR(80) NULL AFTER about_me;
ALTER TABLE profiles ADD COLUMN age TINYINT UNSIGNED NULL AFTER education;
ALTER TABLE profiles ADD COLUMN birthplace VARCHAR(80) NULL AFTER age;
ALTER TABLE profiles ADD COLUMN occupation VARCHAR(80) NULL AFTER birthplace;
