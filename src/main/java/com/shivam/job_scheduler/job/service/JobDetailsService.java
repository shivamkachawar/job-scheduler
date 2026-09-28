package com.shivam.job_scheduler.job.service;

import com.shivam.job_scheduler.job.dto.JobDetailsResponse;
import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class JobDetailsService {

    private final JobRepository jobRepository;

    public JobDetailsService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional(readOnly = true)
    public JobDetailsResponse getJobDetails(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        return toResponse(job);
    }

    private JobDetailsResponse toResponse(Job job) {
        return new JobDetailsResponse(
                job.getId(),
                job.getName(),
                job.getDescription(),
                job.getStatus(),

                job.getScheduleType(),
                job.getScheduleValue(),
                job.getTimezone(),
                job.getStartAt(),
                job.getNextRunAt(),

                job.getHttpMethod(),
                job.getUrl(),
                job.getHeaders(),
                job.getQueryParams(),
                job.getBody(),
                job.getContentType(),
                job.getTimeoutMs(),

                job.getMaxRetries(),
                job.getInitialRetryDelayMs(),
                job.getMaxRetryDelayMs(),

                job.getCreatedAt(),
                job.getUpdatedAt());
    }
}