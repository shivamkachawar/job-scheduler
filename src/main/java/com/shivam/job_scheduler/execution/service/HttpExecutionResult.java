package com.shivam.job_scheduler.execution.service;

import com.shivam.job_scheduler.execution.entity.AttemptErrorType;

public record HttpExecutionResult(
        boolean success,
        Integer httpStatusCode,
        String responseBody,
        long durationMs,
        AttemptErrorType errorType,
        String errorMessage) {
}