package com.shivam.job_scheduler.job.service;

import com.shivam.job_scheduler.job.dto.JobListResponse;
import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.job.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class JobListService {

    private final JobRepository jobRepository;

    public JobListService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional(readOnly = true)
    public Page<JobListResponse> getJobs(UUID userId, Pageable pageable) {
        return jobRepository
                .findByUser_IdOrderByCreatedAtDescIdDesc(userId, pageable)
                .map(this::toResponse);
    }

    private JobListResponse toResponse(Job job) {
        return new JobListResponse(
                job.getId(),
                job.getName(),
                job.getDescription(),
                job.getStatus(),
                job.getScheduleType(),
                job.getScheduleValue(),
                job.getTimezone(),
                job.getNextRunAt(),
                job.getHttpMethod().name(),
                job.getUrl(),
                job.getCreatedAt());
    }
}