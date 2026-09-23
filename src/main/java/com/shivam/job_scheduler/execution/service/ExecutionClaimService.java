package com.shivam.job_scheduler.execution.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shivam.job_scheduler.execution.entity.Execution;
import com.shivam.job_scheduler.execution.entity.ExecutionStatus;

import java.util.Optional;
import com.shivam.job_scheduler.execution.repository.ExecutionRepository;

@Service
public class ExecutionClaimService {
    private final ExecutionRepository executionRepository;

    public ExecutionClaimService(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    @Transactional
    public boolean claimExecution(UUID executionId) {

        int updated = executionRepository.claimExecution(
                executionId,
                Instant.now());

        return updated == 1;
    }

    @Transactional(readOnly = true)
    public Optional<Execution> findPendingExecution() {
        return executionRepository.findFirstByStatusOrderByCreatedAtAsc(
                ExecutionStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Execution findExecutionWithJob(UUID executionId) {

        return executionRepository.findByIdWithJob(executionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Execution not found: " + executionId));
    }
}
