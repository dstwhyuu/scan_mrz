-- ============================================================
-- DDL: Hotel Front-Office Scanner – MySQL 8.x
-- Jalankan sekali di database `hotel_fo_scanner`.
-- ============================================================

-- 1. users
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)     NOT NULL,
    email           VARCHAR(100)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(100)    NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    failed_login_attempts SMALLINT  NOT NULL DEFAULT 0,
    locked_until    DATETIME(6)     NULL,
    last_login_at   DATETIME(6)     NULL,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    deleted_at      DATETIME(6)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. guests
CREATE TABLE IF NOT EXISTS guests (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    document_type        CHAR(1)      NOT NULL DEFAULT 'P',
    passport_number      VARCHAR(255) NOT NULL,
    passport_number_hash VARCHAR(64)  NOT NULL,
    issuing_country      CHAR(3)      NOT NULL,
    surname              VARCHAR(100) NOT NULL,
    given_names          VARCHAR(100) NOT NULL,
    nationality          CHAR(3)      NOT NULL,
    date_of_birth        DATE         NOT NULL,
    gender               CHAR(1)      NOT NULL,
    expiry_date          DATE         NOT NULL,
    mrz_line1            VARCHAR(44)  NULL,
    mrz_line2            VARCHAR(44)  NULL,
    check_digits_valid   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           DATETIME(6)  NOT NULL,
    updated_at           DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. guest_visits
CREATE TABLE IF NOT EXISTS guest_visits (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    guest_id        BIGINT       NOT NULL,
    room_number     VARCHAR(10)  NULL,
    check_in_date   DATE         NOT NULL,
    check_out_date  DATE         NULL,
    purpose_of_stay VARCHAR(100) NULL,
    created_by      BIGINT       NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_visits_guest FOREIGN KEY (guest_id)  REFERENCES guests(id),
    CONSTRAINT fk_visits_user  FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. scan_logs
CREATE TABLE IF NOT EXISTS scan_logs (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    guest_id              BIGINT        NULL,
    user_id               BIGINT        NOT NULL,
    status                VARCHAR(20)   NOT NULL,
    ocr_confidence_score  DECIMAL(5,2)  NULL,
    error_message         VARCHAR(500)  NULL,
    ip_address            VARCHAR(45)   NULL,
    user_agent            VARCHAR(255)  NULL,
    scanned_at            DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_scanlogs_guest FOREIGN KEY (guest_id) REFERENCES guests(id),
    CONSTRAINT fk_scanlogs_user  FOREIGN KEY (user_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
