package com.shivam.job_scheduler.execution.dto;

import com.shivam.job_scheduler.execution.entity.ExecutionStatus;

import java.time.Instant;
import java.util.UUID;

public record ExecutionHistoryResponse(
        UUID id,
        UUID jobId,
        Instant scheduledAt,
        Instant startedAt,
        Instant completedAt,
        ExecutionStatus status,
        String reason,
        Long durationMs) {
}