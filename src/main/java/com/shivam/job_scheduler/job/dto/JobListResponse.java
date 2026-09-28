package com.shivam.job_scheduler.job.dto;

import com.shivam.job_scheduler.job.entity.JobStatus;
import com.shivam.job_scheduler.job.entity.ScheduleType;

import java.time.Instant;
import java.util.UUID;

public record JobListResponse(
        UUID id,
        String name,
        String description,
        JobStatus status,
        ScheduleType scheduleType,
        String scheduleValue,
        String timezone,
        Instant nextRunAt,
        String httpMethod,
        String url,
        Instant createdAt) {
}