package com.shivam.job_scheduler.execution.dto;

import com.shivam.job_scheduler.execution.entity.AttemptErrorType;
import com.shivam.job_scheduler.execution.entity.AttemptStatus;

import java.time.Instant;
import java.util.UUID;

public record ExecutionAttemptResponse(
        UUID id,
        int attemptNumber,
        AttemptStatus status,
        Instant startedAt,
        Instant completedAt,
        Integer httpStatusCode,
        AttemptErrorType errorType,
        String errorMessage,
        String responseBody,
        boolean responseTruncated,
        Long durationMs) {
}