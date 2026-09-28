package com.shivam.job_scheduler.outbox.service;

import com.shivam.job_scheduler.execution.service.ExecutionService;
import com.shivam.job_scheduler.kafka.ExecutionMessage;
import com.shivam.job_scheduler.kafka.KafkaProducer;
import com.shivam.job_scheduler.outbox.entity.OutboxEvent;
import com.shivam.job_scheduler.outbox.repository.OutboxEventRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private ExecutionService executionService;

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    @Test
    void kafkaFailureShouldLeaveEventUnpublished() {
        UUID executionId = UUID.randomUUID();

        OutboxEvent event = new OutboxEvent(
                "EXECUTION_REQUESTED",
                executionId,
                Map.of(
                        "executionId", executionId.toString(),
                        "scheduledAt", Instant.now().toString()));

        when(outboxEventRepository
                .findTop100ByPublishedAtIsNullAndExpiredAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of(event));

        when(kafkaProducer.send(any(ExecutionMessage.class)))
                .thenReturn(CompletableFuture.failedFuture(
                        new RuntimeException("Simulated Kafka failure")));

        outboxPublisher.publishPendingEvents();

        // The event must not be marked as published.
        org.junit.jupiter.api.Assertions.assertNull(event.getPublishedAt());

        // It must remain available for retry.
        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
    }
}