package com.shivam.job_scheduler.execution.service;

import com.shivam.job_scheduler.execution.dto.ExecutionAttemptResponse;
import com.shivam.job_scheduler.execution.dto.ExecutionDetailsResponse;
import com.shivam.job_scheduler.execution.entity.Execution;
import com.shivam.job_scheduler.execution.entity.ExecutionAttempt;
import com.shivam.job_scheduler.execution.repository.ExecutionAttemptRepository;
import com.shivam.job_scheduler.execution.repository.ExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class ExecutionDetailsService {

    private final ExecutionRepository executionRepository;
    private final ExecutionAttemptRepository executionAttemptRepository;

    public ExecutionDetailsService(
            ExecutionRepository executionRepository,
            ExecutionAttemptRepository executionAttemptRepository) {
        this.executionRepository = executionRepository;
        this.executionAttemptRepository = executionAttemptRepository;
    }

    @Transactional(readOnly = true)
    public ExecutionDetailsResponse getExecutionDetails(UUID executionId) {

        Execution execution = executionRepository
                .findByIdWithJob(executionId)
                .orElseThrow(() -> new ExecutionNotFoundException(executionId));

        List<ExecutionAttemptResponse> attempts = executionAttemptRepository
                .findByExecution_IdOrderByAttemptNumberAsc(executionId)
                .stream()
                .map(this::toAttemptResponse)
                .toList();

        return new ExecutionDetailsResponse(
                execution.getId(),
                execution.getJob().getId(),
                execution.getScheduledAt(),
                execution.getStartedAt(),
                execution.getCompletedAt(),
                execution.getStatus(),
                execution.getReason(),
                calculateDuration(
                        execution.getStartedAt(),
                        execution.getCompletedAt()),
                attempts);
    }

    private ExecutionAttemptResponse toAttemptResponse(
            ExecutionAttempt attempt) {

        return new ExecutionAttemptResponse(
                attempt.getId(),
                attempt.getAttemptNumber(),
                attempt.getStatus(),
                attempt.getStartedAt(),
                attempt.getCompletedAt(),
                attempt.getHttpStatusCode(),
                attempt.getErrorType(),
                attempt.getErrorMessage(),
                attempt.getResponseBody(),
                attempt.getResponseTruncated(),
                calculateDuration(
                        attempt.getStartedAt(),
                        attempt.getCompletedAt()));
    }

    private Long calculateDuration(
            java.time.Instant startedAt,
            java.time.Instant completedAt) {

        if (startedAt == null || completedAt == null) {
            return null;
        }

        return Duration.between(startedAt, completedAt).toMillis();
    }
}