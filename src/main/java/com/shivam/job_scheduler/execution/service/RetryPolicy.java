package com.shivam.job_scheduler.execution.service;

import com.shivam.job_scheduler.execution.entity.AttemptErrorType;
import org.springframework.stereotype.Component;

@Component
public class RetryPolicy {

    public boolean shouldRetry(
            AttemptErrorType errorType,
            Integer httpStatusCode,
            int attemptNumber,
            int maxRetries) {

        if (attemptNumber > maxRetries) {
            return false;
        }

        if (httpStatusCode != null) {
            return httpStatusCode >= 500 && httpStatusCode <= 599;
        }

        return errorType == AttemptErrorType.TIMEOUT
                || errorType == AttemptErrorType.CONNECTION_ERROR;
    }

    public long calculateDelay(
            long initialDelayMs,
            long maxDelayMs,
            int attemptNumber) {

        long delay = initialDelayMs * (1L << (attemptNumber - 1));

        return Math.min(delay, maxDelayMs);
    }
}