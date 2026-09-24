package com.shivam.job_scheduler.execution.worker;

import com.shivam.job_scheduler.execution.entity.Execution;
import com.shivam.job_scheduler.execution.entity.ExecutionAttempt;
import com.shivam.job_scheduler.execution.entity.ExecutionStatus;
import com.shivam.job_scheduler.execution.service.ExecutionAttemptService;
import com.shivam.job_scheduler.execution.service.ExecutionClaimService;
import com.shivam.job_scheduler.execution.service.ExecutionService;
import com.shivam.job_scheduler.execution.service.HttpExecutionResult;
import com.shivam.job_scheduler.execution.service.HttpJobExecutor;
import com.shivam.job_scheduler.execution.service.RetryPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ExecutionWorker {

    private static final Logger log = LoggerFactory.getLogger(ExecutionWorker.class);

    private static final long POLL_INTERVAL_MS = 500;

    private final ExecutionClaimService executionClaimService;
    private final ExecutionAttemptService executionAttemptService;
    private final HttpJobExecutor httpJobExecutor;
    private final ExecutionService executionService;
    private final RetryPolicy retryPolicy;

    public ExecutionWorker(
            ExecutionClaimService executionClaimService,
            ExecutionAttemptService executionAttemptService,
            HttpJobExecutor httpJobExecutor,
            ExecutionService executionService,
            RetryPolicy retryPolicy) {

        this.executionClaimService = executionClaimService;
        this.executionAttemptService = executionAttemptService;
        this.httpJobExecutor = httpJobExecutor;
        this.executionService = executionService;
        this.retryPolicy = retryPolicy;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Thread workerThread = new Thread(this::runWorker, "execution-worker");

        workerThread.start();
    }

    private void testWorkerFailure() {
        throw new RuntimeException("Intentional worker failure test");
    }

    private void runWorker() {
        log.info("Execution worker started");

        while (true) {
            try {

                while (true) {
                    Optional<Execution> pendingExecution = executionClaimService.findPendingExecution();

                    if (pendingExecution.isEmpty()) {
                        break;
                    }

                    Execution execution = pendingExecution.get();

                    boolean claimed = executionClaimService.claimExecution(
                            execution.getId());

                    if (claimed) {

                        log.info(
                                "Claimed execution: id={}",
                                execution.getId());

                        Execution executionWithJob = executionClaimService.findExecutionWithJob(
                                execution.getId());

                        ExecutionAttempt currentAttempt = null;

                        try {

                            int attemptNumber = 1;

                            while (true) {

                                currentAttempt = executionAttemptService.startAttempt(
                                        executionWithJob,
                                        attemptNumber);

                                // testWorkerFailure(); // Uncomment this line to test worker failure handling

                                HttpExecutionResult result = httpJobExecutor.execute(
                                        executionWithJob.getJob());

                                executionAttemptService.completeAttempt(
                                        currentAttempt,
                                        result);

                                if (result.success()) {

                                    executionService.completeExecution(
                                            executionWithJob,
                                            ExecutionStatus.SUCCESS);

                                    break;
                                }

                                boolean shouldRetry = retryPolicy.shouldRetry(
                                        result.errorType(),
                                        result.httpStatusCode(),
                                        attemptNumber,
                                        executionWithJob.getMaxRetries());

                                if (!shouldRetry) {

                                    executionService.completeExecution(
                                            executionWithJob,
                                            ExecutionStatus.FAILED);

                                    break;
                                }

                                long delay = retryPolicy.calculateDelay(
                                        executionWithJob.getInitialRetryDelayMs(),
                                        executionWithJob.getMaxRetryDelayMs(),
                                        attemptNumber);

                                log.info(
                                        "Retrying execution: id={}, attempt={}, delayMs={}",
                                        executionWithJob.getId(),
                                        attemptNumber + 1,
                                        delay);

                                try {

                                    Thread.sleep(delay);

                                } catch (InterruptedException e) {

                                    Thread.currentThread().interrupt();

                                    executionService.completeExecution(
                                            executionWithJob,
                                            ExecutionStatus.FAILED);

                                    break;
                                }

                                attemptNumber++;
                            }

                        } catch (Exception e) {

                            log.error(
                                    "Unexpected error while processing execution: id={}",
                                    executionWithJob.getId(),
                                    e);

                            if (currentAttempt != null) {

                                executionAttemptService.failAttempt(
                                        currentAttempt,
                                        e);
                            }

                            executionService.completeExecution(
                                    executionWithJob,
                                    ExecutionStatus.FAILED);
                        }
                    }
                }

                Thread.sleep(POLL_INTERVAL_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Execution worker interrupted");
                break;
            } catch (Exception e) {
                log.error("Unexpected error in execution worker", e);
            }
        }
    }
}