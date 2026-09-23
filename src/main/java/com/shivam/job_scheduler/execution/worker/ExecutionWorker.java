package com.shivam.job_scheduler.execution.worker;

import com.shivam.job_scheduler.execution.entity.Execution;
import com.shivam.job_scheduler.execution.entity.ExecutionAttempt;
import com.shivam.job_scheduler.execution.entity.ExecutionStatus;
import com.shivam.job_scheduler.execution.service.ExecutionAttemptService;
import com.shivam.job_scheduler.execution.service.ExecutionClaimService;
import com.shivam.job_scheduler.execution.service.ExecutionService;
import com.shivam.job_scheduler.execution.service.HttpExecutionResult;
import com.shivam.job_scheduler.execution.service.HttpJobExecutor;

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

    public ExecutionWorker(
            ExecutionClaimService executionClaimService,
            ExecutionAttemptService executionAttemptService,
            HttpJobExecutor httpJobExecutor,
            ExecutionService executionService) {

        this.executionClaimService = executionClaimService;
        this.executionAttemptService = executionAttemptService;
        this.httpJobExecutor = httpJobExecutor;
        this.executionService = executionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Thread workerThread = new Thread(this::runWorker, "execution-worker");

        workerThread.start();
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

                        ExecutionAttempt attempt = executionAttemptService.startAttempt(
                                executionWithJob,
                                1);

                        HttpExecutionResult result = httpJobExecutor.execute(
                                executionWithJob.getJob());

                        executionAttemptService.completeAttempt(
                                attempt,
                                result);

                        executionService.completeExecution(
                                execution,
                                result.success()
                                        ? ExecutionStatus.SUCCESS
                                        : ExecutionStatus.FAILED);
                    }
                }

                Thread.sleep(POLL_INTERVAL_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Execution worker interrupted");
                break;
            }
        }
    }
}