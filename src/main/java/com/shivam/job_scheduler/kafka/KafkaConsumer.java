package com.shivam.job_scheduler.kafka;

import com.shivam.job_scheduler.execution.service.ExecutionClaimService;
import com.shivam.job_scheduler.execution.worker.ExecutionProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final ExecutionClaimService executionClaimService;
    private final ExecutionProcessor executionProcessor;

    public KafkaConsumer(
            ExecutionClaimService executionClaimService,
            ExecutionProcessor executionProcessor) {

        this.executionClaimService = executionClaimService;
        this.executionProcessor = executionProcessor;
    }

    @KafkaListener(topics = "job-executions", groupId = "job-workers")
    public void consume(ExecutionMessage message) {

        var executionId = message.executionId();

        log.info("Received execution ID: {}", executionId);

        boolean claimed = executionClaimService.claimExecution(executionId);

        if (!claimed) {
            log.info(
                    "Execution already claimed or no longer pending: id={}",
                    executionId);
            return;
        }

        log.info("Claimed execution: id={}", executionId);

        executionProcessor.process(executionId);
    }
}