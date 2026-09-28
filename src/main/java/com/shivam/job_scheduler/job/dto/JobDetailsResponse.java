package com.shivam.job_scheduler.job.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.shivam.job_scheduler.job.entity.HttpMethod;
import com.shivam.job_scheduler.job.entity.JobStatus;
import com.shivam.job_scheduler.job.entity.ScheduleType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record JobDetailsResponse(
        UUID id,
        String name,
        String description,
        JobStatus status,

        ScheduleType scheduleType,
        String scheduleValue,
        String timezone,
        Instant startAt,
        Instant nextRunAt,

        HttpMethod httpMethod,
        String url,
        Map<String, String> headers,
        Map<String, String> queryParams,
        JsonNode body,
        String contentType,
        Integer timeoutMs,

        Integer maxRetries,
        Long initialRetryDelayMs,
        Long maxRetryDelayMs,

        Instant createdAt,
        Instant updatedAt) {
}