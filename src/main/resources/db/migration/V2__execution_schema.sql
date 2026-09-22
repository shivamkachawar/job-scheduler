CREATE TABLE executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    job_id UUID NOT NULL,

    scheduled_at TIMESTAMPTZ NOT NULL,

    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL,

    max_retries INTEGER NOT NULL,
    initial_retry_delay_ms BIGINT NOT NULL,
    max_retry_delay_ms BIGINT NOT NULL,

    reason TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_executions_job
        FOREIGN KEY (job_id)
        REFERENCES jobs(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_executions_status
        CHECK (
            status IN (
                'PENDING',
                'RUNNING',
                'SUCCESS',
                'FAILED',
                'SKIPPED',
                'MISSED'
            )
        ),

    CONSTRAINT chk_executions_max_retries
        CHECK (max_retries >= 0),

    CONSTRAINT chk_executions_initial_retry_delay
        CHECK (initial_retry_delay_ms > 0),

    CONSTRAINT chk_executions_max_retry_delay
        CHECK (max_retry_delay_ms >= initial_retry_delay_ms)
);

CREATE INDEX idx_executions_job_id
    ON executions (job_id);

CREATE INDEX idx_executions_job_scheduled_at
    ON executions (job_id, scheduled_at);

CREATE INDEX idx_executions_status
    ON executions (status);


CREATE TABLE execution_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    execution_id UUID NOT NULL,

    attempt_number INTEGER NOT NULL,

    status VARCHAR(20) NOT NULL,

    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,

    http_status_code INTEGER,

    error_type VARCHAR(30),
    error_message TEXT,

    response_body TEXT,
    response_truncated BOOLEAN NOT NULL DEFAULT FALSE,

    duration_ms BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_execution_attempts_execution
        FOREIGN KEY (execution_id)
        REFERENCES executions(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_execution_attempts_status
        CHECK (
            status IN (
                'RUNNING',
                'SUCCESS',
                'FAILED'
            )
        ),

    CONSTRAINT chk_execution_attempts_number
        CHECK (attempt_number >= 1),

    CONSTRAINT chk_execution_attempts_http_status
        CHECK (
            http_status_code IS NULL
            OR (
                http_status_code >= 100
                AND http_status_code <= 599
            )
        ),

    CONSTRAINT chk_execution_attempts_duration
        CHECK (
            duration_ms IS NULL
            OR duration_ms >= 0
        ),

    CONSTRAINT uq_execution_attempt_number
        UNIQUE (execution_id, attempt_number)
);