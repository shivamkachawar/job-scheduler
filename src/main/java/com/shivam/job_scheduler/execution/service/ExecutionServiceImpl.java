package com.shivam.job_scheduler.execution.service;

import com.shivam.job_scheduler.execution.entity.Execution;
import com.shivam.job_scheduler.execution.entity.ExecutionStatus;
import com.shivam.job_scheduler.execution.repository.ExecutionRepository;
import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.outbox.repository.OutboxEventRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionRepository executionRepository;
    private final OutboxEventRepository outboxEventRepository;

    public ExecutionServiceImpl(ExecutionRepository executionRepository, OutboxEventRepository outboxEventRepository) {
        this.executionRepository = executionRepository;
        this.outboxEventRepository = outboxEventRepository;
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

    @Override
    @Transactional
    public boolean markMissedAndExpire(UUID executionId) {

        Instant now = Instant.now();

        int updatedExecutions = executionRepository.markMissedIfPending(
                executionId,
                ExecutionStatus.PENDING,
                ExecutionStatus.MISSED,
                "DELIVERY_DEADLINE_EXCEEDED",
                now,
                now.minusSeconds(60));

        if (updatedExecutions == 0) {
            return false;
        }

        int expiredEvents = outboxEventRepository.expirePendingEvent(executionId, now);

        if (expiredEvents > 0) {
            System.out.println("Expired pending outbox event for execution: " + executionId);
        }

        return true;
    }
}