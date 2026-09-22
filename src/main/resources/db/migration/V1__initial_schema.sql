-- ============================================================
-- V1 - Initial Schema
-- Job Scheduler
-- ============================================================

-- Required for UUID generation using gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(255) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    name VARCHAR(100) NOT NULL,

    role VARCHAR(20) NOT NULL DEFAULT 'USER',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_users_role
        CHECK (role IN ('USER', 'ADMIN')),

    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'DISABLED'))
);


-- Email should be unique regardless of case.
-- Example:
-- Shivam@example.com
-- shivam@example.com
-- should represent the same account.
CREATE UNIQUE INDEX uq_users_email_lower
    ON users (LOWER(email));


-- ============================================================
-- JOBS
-- ============================================================

CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    name VARCHAR(150) NOT NULL,

    description TEXT,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    schedule_type VARCHAR(20) NOT NULL,

    schedule_value TEXT NOT NULL,

    timezone VARCHAR(100) NOT NULL,

    next_run_at TIMESTAMPTZ,

    http_method VARCHAR(10) NOT NULL,

    url TEXT NOT NULL,

    headers JSONB NOT NULL DEFAULT '{}'::jsonb,

    query_params JSONB NOT NULL DEFAULT '{}'::jsonb,

    body JSONB,

    content_type VARCHAR(100),

    timeout_ms INTEGER NOT NULL DEFAULT 30000,

    max_retries INTEGER NOT NULL DEFAULT 0,

    initial_retry_delay_ms BIGINT NOT NULL DEFAULT 2000,

    max_retry_delay_ms BIGINT NOT NULL DEFAULT 30000,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,


    -- ========================================================
    -- FOREIGN KEY
    -- ========================================================

    CONSTRAINT fk_jobs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,


    -- ========================================================
    -- CHECK CONSTRAINTS
    -- ========================================================

    CONSTRAINT chk_jobs_status
        CHECK (status IN ('ACTIVE', 'PAUSED')),

    CONSTRAINT chk_jobs_schedule_type
        CHECK (
            schedule_type IN (
                'ONE_TIME',
                'INTERVAL',
                'CRON'
            )
        ),

    CONSTRAINT chk_jobs_http_method
        CHECK (
            http_method IN (
                'GET',
                'POST',
                'PUT',
                'PATCH',
                'DELETE'
            )
        ),

    CONSTRAINT chk_jobs_timeout
        CHECK (timeout_ms > 0),

    CONSTRAINT chk_jobs_max_retries
        CHECK (max_retries >= 0),

    CONSTRAINT chk_jobs_initial_retry_delay
        CHECK (initial_retry_delay_ms > 0),

    CONSTRAINT chk_jobs_max_retry_delay
        CHECK (
            max_retry_delay_ms >= initial_retry_delay_ms
        )
);


-- ============================================================
-- INDEXES
-- ============================================================

-- Used when retrieving jobs belonging to a user.
CREATE INDEX idx_jobs_user_id
    ON jobs (user_id);


-- Used by the scheduler to find active jobs
-- ordered by next_run_at.
CREATE INDEX idx_jobs_scheduler
    ON jobs (status, next_run_at);