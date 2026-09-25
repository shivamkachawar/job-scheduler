package com.shivam.job_scheduler.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, ExecutionMessage> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, ExecutionMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ExecutionMessage message) {
        kafkaTemplate.send("job-executions", message);
    }
}