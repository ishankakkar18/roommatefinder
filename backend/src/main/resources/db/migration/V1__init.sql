-- ============================================================================
-- V1__init.sql
-- Flatmate matcher — core schema
-- MySQL 8.4 · InnoDB · utf8mb4 · all timestamps UTC DATETIME(3)
--
-- Design notes:
--  * messages.id is BIGINT AUTO_INCREMENT (not UUID): sequential InnoDB inserts,
--    and it gives a total order. client_msg_id CHAR(36) is the idempotency key.
--  * conversations.user_a_id < user_b_id (canonical order) so a pair can only
--    ever have one conversation row.
--  * area_adjacency stores one canonical direction (area_a_id < area_b_id).
--  * No free-text column is ever used for matching. about_me is display-only.
-- ============================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- Reference data
-- ---------------------------------------------------------------------------
CREATE TABLE cities (
    id         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    name       VARCHAR(64)  NOT NULL,
    slug       VARCHAR(64)  NOT NULL,
    created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_city_slug (slug)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE areas (
    id         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    city_id    INT UNSIGNED NOT NULL,
    name       VARCHAR(64)  NOT NULL,
    slug       VARCHAR(64)  NOT NULL,
    created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_area_city_slug (city_id, slug),
    KEY idx_area_city (city_id),
    CONSTRAINT fk_area_city FOREIGN KEY (city_id) REFERENCES cities (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Canonical direction only: the smaller area id goes in area_a_id.
-- "adjacent to X" is queried as:
--   SELECT area_b_id FROM area_adjacency WHERE area_a_id = X
--   UNION
--   SELECT area_a_id FROM area_adjacency WHERE area_b_id = X
CREATE TABLE area_adjacency (
    area_a_id INT UNSIGNED NOT NULL,
    area_b_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (area_a_id, area_b_id),
    CONSTRAINT ck_adj_order CHECK (area_a_id < area_b_id),
    CONSTRAINT fk_adj_a FOREIGN KEY (area_a_id) REFERENCES areas (id) ON DELETE CASCADE,
    CONSTRAINT fk_adj_b FOREIGN KEY (area_b_id) REFERENCES areas (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------------
-- Users & auth
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    phone             VARCHAR(15)  NOT NULL,
    name              VARCHAR(40)  NULL,
    gender            VARCHAR(16)  NOT NULL,
    status            VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    phone_verified_at DATETIME(3)  NULL,
    last_login_at     DATETIME(3)  NULL,
    created_at        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at        DATETIME(3)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_phone (phone),
    CONSTRAINT ck_user_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'UNDISCLOSED')),
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Never store the raw OTP. code_hash = SHA-256(phone || code || server_pepper).
CREATE TABLE otp_codes (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    phone       VARCHAR(15)     NOT NULL,
    code_hash   CHAR(64)        NOT NULL,
    expires_at  DATETIME(3)     NOT NULL,
    attempts    TINYINT UNSIGNED NOT NULL DEFAULT 0,
    consumed_at DATETIME(3)     NULL,
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_otp_phone_created (phone, created_at DESC),
    KEY idx_otp_expiry (expires_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Refresh-token rotation: on refresh, the old row is revoked and replaced_by
-- points at the new row. A reused revoked token means theft -> revoke the chain.
CREATE TABLE refresh_tokens (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id     BIGINT UNSIGNED NOT NULL,
    token_hash  CHAR(64)        NOT NULL,
    expires_at  DATETIME(3)     NOT NULL,
    revoked_at  DATETIME(3)     NULL,
    replaced_by BIGINT UNSIGNED NULL,
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_hash (token_hash),
    KEY idx_refresh_user (user_id),
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------------
-- Onboarding profile — every matching input is an enum or a small int
-- ---------------------------------------------------------------------------
CREATE TABLE profiles (
    user_id              BIGINT UNSIGNED  NOT NULL,
    looking_for          VARCHAR(24)      NOT NULL,
    city_id              INT UNSIGNED     NOT NULL,
    area_id              INT UNSIGNED     NOT NULL,
    budget_band          TINYINT UNSIGNED NOT NULL,
    move_in              VARCHAR(16)      NOT NULL,
    flatmate_gender_pref VARCHAR(16)      NOT NULL,
    sleep_time           VARCHAR(16)      NOT NULL,
    wake_time            VARCHAR(16)      NOT NULL,
    cleanliness          TINYINT UNSIGNED NOT NULL,
    guests               VARCHAR(16)      NOT NULL,
    social               VARCHAR(16)      NOT NULL,
    food                 VARCHAR(8)       NOT NULL,
    cooking              VARCHAR(16)      NOT NULL,
    work_from_home       VARCHAR(16)      NOT NULL DEFAULT 'NEVER',
    about_me             VARCHAR(200)     NULL,
    smoke                VARCHAR(8)       NOT NULL,
    smoke_tolerance      VARCHAR(16)      NOT NULL,
    drink                VARCHAR(16)      NOT NULL,
    drink_tolerance      VARCHAR(16)      NOT NULL,
    pets                 VARCHAR(8)       NOT NULL,
    pet_tolerance        VARCHAR(16)      NOT NULL,
    completed_at         DATETIME(3)      NULL,
    created_at           DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at           DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id),
    KEY idx_profile_pool (city_id, area_id, budget_band),
    KEY idx_profile_area (area_id),
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_profile_city FOREIGN KEY (city_id) REFERENCES cities (id),
    CONSTRAINT fk_profile_area FOREIGN KEY (area_id) REFERENCES areas (id),
    CONSTRAINT ck_prof_budget CHECK (budget_band BETWEEN 0 AND 7),
    CONSTRAINT ck_prof_clean  CHECK (cleanliness BETWEEN 1 AND 5),
    CONSTRAINT ck_prof_looking CHECK (looking_for IN ('ROOM_SHARE', 'FLATMATE_FOR_MY_FLAT', 'BOTH')),
    CONSTRAINT ck_prof_movein  CHECK (move_in IN ('IMMEDIATE', 'TWO_WEEKS', 'ONE_MONTH', 'TWO_MONTHS', 'EXPLORING')),
    CONSTRAINT ck_prof_genderpref CHECK (flatmate_gender_pref IN ('SAME_AS_MINE', 'NO_PREF', 'MALE_ONLY', 'FEMALE_ONLY')),
    CONSTRAINT ck_prof_sleep   CHECK (sleep_time IN ('BEFORE_10', '22_23', '23_24', 'AFTER_MIDNIGHT')),
    CONSTRAINT ck_prof_wake    CHECK (wake_time IN ('BEFORE_6', '6_8', '8_10', 'AFTER_10')),
    CONSTRAINT ck_prof_guests  CHECK (guests IN ('RARELY', 'SOMETIMES', 'OFTEN', 'VERY_OFTEN')),
    CONSTRAINT ck_prof_social  CHECK (social IN ('QUIET', 'BALANCED', 'SOCIAL')),
    CONSTRAINT ck_prof_food    CHECK (food IN ('VEG', 'EGG', 'NONVEG', 'VEGAN')),
    CONSTRAINT ck_prof_cooking CHECK (cooking IN ('NEVER', 'RARELY', 'SOMETIMES', 'DAILY')),
    CONSTRAINT ck_prof_wfh     CHECK (work_from_home IN ('NEVER', '1_2', '3_4', 'ALWAYS')),
    CONSTRAINT ck_prof_smoke   CHECK (smoke IN ('I_DONT', 'I_DO', 'QUIT')),
    CONSTRAINT ck_prof_smoketol CHECK (smoke_tolerance IN ('CANT', 'OUTDOORS_ONLY', 'DONT_MIND')),
    CONSTRAINT ck_prof_drink   CHECK (drink IN ('NEVER', 'SOCIALLY', 'REGULARLY')),
    CONSTRAINT ck_prof_drinktol CHECK (drink_tolerance IN ('DONT_MIND', 'RATHER_NOT')),
    CONSTRAINT ck_prof_pets    CHECK (pets IN ('NONE', 'HAVE', 'WANT')),
    CONSTRAINT ck_prof_pettol  CHECK (pet_tolerance IN ('DONT_MIND', 'NO_PETS'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------------
-- Match requests — the pool-exclusion rule lives here
--   Any pair with status PENDING/ACCEPTED/DECLINED is excluded both ways.
--   WITHDRAWN/EXPIRED becomes re-requestable after 7 days.
-- ---------------------------------------------------------------------------
CREATE TABLE match_requests (
    id               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    from_user_id     BIGINT UNSIGNED  NOT NULL,
    to_user_id       BIGINT UNSIGNED  NOT NULL,
    status           VARCHAR(16)      NOT NULL DEFAULT 'PENDING',
    score_at_request TINYINT UNSIGNED NOT NULL,
    created_at       DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    responded_at     DATETIME(3)      NULL,
    expires_at       DATETIME(3)      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_request_pair (from_user_id, to_user_id),
    KEY idx_request_incoming (to_user_id, status, created_at DESC),
    KEY idx_request_outgoing (from_user_id, status),
    KEY idx_request_expiry (status, expires_at),
    CONSTRAINT ck_request_no_self CHECK (from_user_id <> to_user_id),
    CONSTRAINT ck_request_score CHECK (score_at_request BETWEEN 0 AND 100),
    CONSTRAINT ck_request_status CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'WITHDRAWN', 'EXPIRED')),
    CONSTRAINT fk_request_from FOREIGN KEY (from_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_request_to   FOREIGN KEY (to_user_id)   REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------------
-- Conversations — created only when a request is ACCEPTED
-- ---------------------------------------------------------------------------
CREATE TABLE conversations (
    id              CHAR(36)        NOT NULL,
    request_id      BIGINT UNSIGNED NOT NULL,
    user_a_id       BIGINT UNSIGNED NOT NULL,
    user_b_id       BIGINT UNSIGNED NOT NULL,
    status          VARCHAR(16)     NOT NULL DEFAULT 'ACCEPTED',
    last_message_at DATETIME(3)     NULL,
    created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_conv_request (request_id),
    UNIQUE KEY uk_conv_pair (user_a_id, user_b_id),
    KEY idx_conv_a (user_a_id, last_message_at DESC),
    KEY idx_conv_b (user_b_id, last_message_at DESC),
    CONSTRAINT ck_conv_order CHECK (user_a_id < user_b_id),
    CONSTRAINT ck_conv_status CHECK (status IN ('ACCEPTED', 'ARCHIVED', 'BLOCKED')),
    CONSTRAINT fk_conv_request FOREIGN KEY (request_id) REFERENCES match_requests (id),
    CONSTRAINT fk_conv_a FOREIGN KEY (user_a_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_b FOREIGN KEY (user_b_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------------
-- Messages — idempotent via (conversation_id, client_msg_id)
-- ---------------------------------------------------------------------------
CREATE TABLE messages (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    client_msg_id   CHAR(36)        NOT NULL,
    conversation_id CHAR(36)        NOT NULL,
    sender_id       BIGINT UNSIGNED NOT NULL,
    body            VARCHAR(2000)   NOT NULL,
    created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at      DATETIME(3)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_msg_client (conversation_id, client_msg_id),
    KEY idx_msg_conv_id (conversation_id, id),
    KEY idx_msg_sender (sender_id),
    CONSTRAINT fk_msg_conv FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_msg_body_len CHECK (CHAR_LENGTH(body) BETWEEN 1 AND 2000)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Unread is DERIVED from this cursor. There is no unread counter column.
CREATE TABLE read_cursors (
    conversation_id      CHAR(36)        NOT NULL,
    user_id              BIGINT UNSIGNED NOT NULL,
    last_read_message_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
    updated_at           DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT fk_cursor_conv FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_cursor_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
