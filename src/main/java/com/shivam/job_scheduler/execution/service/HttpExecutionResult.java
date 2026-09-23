package com.shivam.job_scheduler.execution.service;

public record HttpExecutionResult(
        boolean success,
        Integer httpStatusCode,
        String responseBody,
        long durationMs) {
}