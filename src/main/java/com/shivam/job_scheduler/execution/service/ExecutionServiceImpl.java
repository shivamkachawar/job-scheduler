package com.shivam.job_scheduler.execution.service;

import com.shivam.job_scheduler.execution.entity.Execution;
import com.shivam.job_scheduler.execution.entity.ExecutionStatus;
import com.shivam.job_scheduler.execution.repository.ExecutionRepository;
import com.shivam.job_scheduler.job.entity.Job;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionRepository executionRepository;

    public ExecutionServiceImpl(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    @Override
    @Transactional
    public Execution createExecution(Job job) {
        Instant now = Instant.now();

        Execution execution = new Execution();

        execution.setJob(job);
        execution.setScheduledAt(job.getNextRunAt());
        execution.setStatus(ExecutionStatus.PENDING);

        execution.setMaxRetries(job.getMaxRetries());
        execution.setInitialRetryDelayMs(job.getInitialRetryDelayMs());
        execution.setMaxRetryDelayMs(job.getMaxRetryDelayMs());

        execution.setCreatedAt(now);
        execution.setUpdatedAt(now);

        return executionRepository.save(execution);
    }

    @Override
    @Transactional
    public Execution createMissedExecution(Job job, String reason) {
        Instant now = Instant.now();

        Execution execution = new Execution();

        execution.setJob(job);
        execution.setScheduledAt(job.getNextRunAt());
        execution.setStatus(ExecutionStatus.MISSED);

        execution.setMaxRetries(job.getMaxRetries());
        execution.setInitialRetryDelayMs(job.getInitialRetryDelayMs());
        execution.setMaxRetryDelayMs(job.getMaxRetryDelayMs());

        execution.setReason(reason);

        execution.setCreatedAt(now);
        execution.setUpdatedAt(now);

        return executionRepository.save(execution);
    }

    @Override
    @Transactional
    public void completeExecution(Execution execution, ExecutionStatus status) {

        execution.setStatus(status);
        execution.setCompletedAt(Instant.now());
        execution.setUpdatedAt(Instant.now());

        executionRepository.save(execution);
    }

}