package com.shivam.job_scheduler.execution.service;

import com.shivam.job_scheduler.execution.entity.AttemptStatus;
import com.shivam.job_scheduler.execution.entity.Execution;
import com.shivam.job_scheduler.execution.entity.ExecutionAttempt;
import com.shivam.job_scheduler.execution.repository.ExecutionAttemptRepository;

import java.time.Instant;

import org.springframework.stereotype.Service;

@Service
public class ExecutionAttemptService {

    private final ExecutionAttemptRepository executionAttemptRepository;

    public ExecutionAttemptService(
            ExecutionAttemptRepository executionAttemptRepository) {
        this.executionAttemptRepository = executionAttemptRepository;
    }

    public ExecutionAttempt startAttempt(
            Execution execution,
            int attemptNumber) {

        ExecutionAttempt attempt = new ExecutionAttempt();

        attempt.setExecution(execution);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setStatus(AttemptStatus.RUNNING);
        attempt.setStartedAt(Instant.now());
        attempt.setResponseTruncated(false);

        return executionAttemptRepository.save(attempt);
    }

    public void completeAttempt(
            ExecutionAttempt attempt,
            HttpExecutionResult result) {

        attempt.setStatus(
                result.success()
                        ? AttemptStatus.SUCCESS
                        : AttemptStatus.FAILED);

        attempt.setCompletedAt(Instant.now());
        attempt.setHttpStatusCode(result.httpStatusCode());
        attempt.setResponseBody(result.responseBody());
        attempt.setDurationMs(result.durationMs());
        attempt.setErrorType(result.errorType());
        attempt.setErrorMessage(result.errorMessage());

        executionAttemptRepository.save(attempt);
    }
}