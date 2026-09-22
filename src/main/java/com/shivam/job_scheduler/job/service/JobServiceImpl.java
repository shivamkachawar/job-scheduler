package com.shivam.job_scheduler.job.service;

import com.shivam.job_scheduler.job.repository.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.shivam.job_scheduler.job.dto.CreateJobRequest;
import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.job.entity.JobStatus;
import com.shivam.job_scheduler.scheduling.ScheduleCalculator;
import com.shivam.job_scheduler.user.entity.User;
import com.shivam.job_scheduler.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class JobServiceImpl implements JobService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ScheduleCalculator scheduleCalculator;

    public JobServiceImpl(
            UserRepository userRepository,
            JobRepository jobRepository,
            ScheduleCalculator scheduleCalculator) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.scheduleCalculator = scheduleCalculator;

    }

    @Override
    @Transactional
    public Job createJob(UUID userId, CreateJobRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId));

        validateTimezone(request.timezone());

        validateUrl(request.url());

        validateRetryConfiguration(
                request.initialRetryDelayMs(),
                request.maxRetryDelayMs());

        Instant now = Instant.now();

        Instant nextRunAt = scheduleCalculator.calculateNext(
                request.scheduleType(),
                request.scheduleValue(),
                request.timezone(),
                now);

        if (request.scheduleType().name().equals("ONE_TIME")
                && !nextRunAt.isAfter(now)) {

            throw new IllegalArgumentException(
                    "ONE_TIME schedule must be in the future");
        }

        Job job = new Job();

        job.setUser(user);
        job.setName(request.name());
        job.setDescription(request.description());

        job.setStatus(JobStatus.ACTIVE);

        job.setScheduleType(request.scheduleType());
        job.setScheduleValue(request.scheduleValue());
        job.setTimezone(request.timezone());

        job.setNextRunAt(nextRunAt);

        job.setHttpMethod(request.httpMethod());
        job.setUrl(request.url());

        job.setHeaders(
                request.headers() == null
                        ? Collections.emptyMap()
                        : request.headers());

        job.setQueryParams(
                request.queryParams() == null
                        ? Collections.emptyMap()
                        : request.queryParams());

        job.setBody(request.body());

        job.setContentType(request.contentType());

        job.setTimeoutMs(request.timeoutMs());
        job.setMaxRetries(request.maxRetries());
        job.setInitialRetryDelayMs(request.initialRetryDelayMs());
        job.setMaxRetryDelayMs(request.maxRetryDelayMs());

        return jobRepository.save(job);
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid timezone: " + timezone);
        }
    }

    private void validateUrl(String url) {
        try {
            URI uri = URI.create(url);

            String scheme = uri.getScheme();

            if (scheme == null
                    || !(scheme.equalsIgnoreCase("http")
                            || scheme.equalsIgnoreCase("https"))) {

                throw new IllegalArgumentException(
                        "URL must use HTTP or HTTPS");
            }

            if (uri.getHost() == null) {
                throw new IllegalArgumentException(
                        "URL must contain a valid host");
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid URL: " + url,
                    e);
        }
    }

    private void validateRetryConfiguration(
            Long initialRetryDelayMs,
            Long maxRetryDelayMs) {
        if (maxRetryDelayMs < initialRetryDelayMs) {
            throw new IllegalArgumentException(
                    "maxRetryDelayMs must be greater than or equal to " +
                            "initialRetryDelayMs");
        }
    }
}