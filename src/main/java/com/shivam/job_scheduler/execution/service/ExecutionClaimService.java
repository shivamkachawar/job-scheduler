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
    private final ExecutionService executionService;

    public ExecutionClaimService(ExecutionRepository executionRepository, ExecutionService executionService) {
        this.executionRepository = executionRepository;
        this.executionService = executionService;
    }

    @Transactional
    public boolean claimExecution(UUID executionId) {

        Instant now = Instant.now();
        Instant deadlineCutoff = now.minusSeconds(60);

        int updated = executionRepository.claimExecution(
                executionId,
                now,
                deadlineCutoff);

        // Log the result of the conditional claim.
        System.out.println(
                "Claim result for " + executionId + ": updated=" + updated);

        if (updated == 1) {
            return true;
        }

        // Claim failed. Check whether the execution has expired.
        System.out.println(
                "Claim failed. Checking deadline for: " + executionId);
        boolean markedMissed = executionService.markMissedAndExpire(executionId);

        return false;
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
