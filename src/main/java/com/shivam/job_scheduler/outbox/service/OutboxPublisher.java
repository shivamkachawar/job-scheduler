package com.shivam.job_scheduler.outbox.service;

import com.shivam.job_scheduler.kafka.KafkaProducer;
import com.shivam.job_scheduler.kafka.ExecutionMessage;
import com.shivam.job_scheduler.outbox.entity.OutboxEvent;
import com.shivam.job_scheduler.outbox.repository.OutboxEventRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducer kafkaProducer;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaProducer kafkaProducer) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {

        List<OutboxEvent> events = outboxEventRepository
                .findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            try {
                UUID executionId = UUID.fromString(
                        event.getPayload()
                                .get("executionId")
                                .toString());

                ExecutionMessage message = new ExecutionMessage(executionId);

                // Wait for Kafka's acknowledgement.
                kafkaProducer.send(message).join();

                // Only mark published after successful acknowledgement.
                event.setPublishedAt(Instant.now());
                outboxEventRepository.save(event);

            } catch (Exception e) {
                // Leave the event unpublished so it can be retried.
                System.err.println(
                        "Failed to publish outbox event "
                                + event.getId() + ": " + e.getMessage());
            }
        }
    }
}