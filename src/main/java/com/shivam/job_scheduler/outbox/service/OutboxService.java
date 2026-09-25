package com.shivam.job_scheduler.outbox.service;

import com.shivam.job_scheduler.outbox.entity.OutboxEvent;
import com.shivam.job_scheduler.outbox.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.time.Instant;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    public void createExecutionRequestedEvent(UUID executionId, Instant scheduledAt) {

        OutboxEvent event = new OutboxEvent(
                "EXECUTION_REQUESTED",
                executionId,
                Map.of(
                        "executionId", executionId.toString(),
                        "scheduledAt", scheduledAt.toString()));

        outboxEventRepository.save(event);
    }
}