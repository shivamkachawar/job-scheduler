package com.shivam.job_scheduler.outbox.service;

import com.shivam.job_scheduler.outbox.entity.OutboxEvent;
import com.shivam.job_scheduler.outbox.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    public void createExecutionRequestedEvent(UUID executionId) {

        OutboxEvent event = new OutboxEvent(
                "EXECUTION_REQUESTED",
                executionId,
                Map.of("executionId", executionId.toString()));

        outboxEventRepository.save(event);
    }
}