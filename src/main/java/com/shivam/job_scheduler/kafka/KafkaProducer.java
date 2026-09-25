package com.shivam.job_scheduler.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, ExecutionMessage> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, ExecutionMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, ExecutionMessage>> send(
            ExecutionMessage message) {

        return kafkaTemplate.send("job-executions", message);
    }
}