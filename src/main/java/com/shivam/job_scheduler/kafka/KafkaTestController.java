package com.shivam.job_scheduler.kafka;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaTestController {

    private final KafkaProducer kafkaProducer;

    public KafkaTestController(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping("/test/kafka")
    public String sendMessage(@RequestParam UUID executionId) {
        kafkaProducer.send(new ExecutionMessage(executionId));
        return "Message sent";
    }
}