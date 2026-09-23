package com.shivam.job_scheduler.job.dto;

import com.shivam.job_scheduler.job.entity.HttpMethod;
import com.shivam.job_scheduler.job.entity.ScheduleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Map;

public record CreateJobRequest(

                @NotBlank @Size(max = 150) String name,

                String description,

                @NotNull ScheduleType scheduleType,

                @NotBlank String scheduleValue,

                @NotBlank String timezone,

                Instant startAt,

                @NotNull HttpMethod httpMethod,

                @NotBlank String url,

                Map<String, String> headers,

                Map<String, String> queryParams,

                JsonNode body,

                @Size(max = 100) String contentType,

                @NotNull @Min(1) Integer timeoutMs,

                @NotNull @Min(0) Integer maxRetries,

                @NotNull @Min(1) Long initialRetryDelayMs,

                @NotNull @Min(1) Long maxRetryDelayMs) {
}