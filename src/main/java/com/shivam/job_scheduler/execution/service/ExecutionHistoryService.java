package com.shivam.job_scheduler.execution.service;

import com.shivam.job_scheduler.execution.dto.ExecutionHistoryResponse;
import com.shivam.job_scheduler.execution.entity.Execution;
import com.shivam.job_scheduler.execution.repository.ExecutionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class ExecutionHistoryService {

    private final ExecutionRepository executionRepository;

    public ExecutionHistoryService(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    public Page<ExecutionHistoryResponse> getExecutionHistory(
            UUID jobId,
            Pageable pageable) {

        return executionRepository
                .findByJob_IdOrderByScheduledAtDescIdDesc(jobId, pageable)
                .map(this::toResponse);
    }

    private ExecutionHistoryResponse toResponse(Execution execution) {

        Long durationMs = null;

        if (execution.getStartedAt() != null
                && execution.getCompletedAt() != null) {

            durationMs = Duration.between(
                    execution.getStartedAt(),
                    execution.getCompletedAt()).toMillis();
        }

        return new ExecutionHistoryResponse(
                execution.getId(),
                execution.getJob().getId(),
                execution.getScheduledAt(),
                execution.getStartedAt(),
                execution.getCompletedAt(),
                execution.getStatus(),
                execution.getReason(),
                durationMs);
    }
}