package com.shivam.job_scheduler.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class TestRetryController {

    private final AtomicInteger requestCount = new AtomicInteger();

    @GetMapping("/test/retry")
    public ResponseEntity<String> retryTest() {

        int count = requestCount.incrementAndGet();

        if (count <= 2) {
            return ResponseEntity
                    .internalServerError()
                    .body("Temporary failure - attempt " + count);
        }

        return ResponseEntity.ok("Success - attempt " + count);
    }

    @PostMapping("/test/retry/reset")
    public String resetRetryTest() {
        requestCount.set(0);
        return "Retry counter reset";
    }
}