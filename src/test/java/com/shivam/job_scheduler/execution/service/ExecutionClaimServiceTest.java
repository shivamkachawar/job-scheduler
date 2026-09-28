package com.shivam.job_scheduler.execution.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Testcontainers
class ExecutionClaimServiceTest {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExecutionClaimService executionClaimService;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void onlyOneConcurrentClaimShouldSucceed() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();

        // Create a user
        jdbcTemplate.update("""
                INSERT INTO users
                    (id, email, password_hash, name, role, status,
                     created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                userId,
                "test-" + userId + "@example.com",
                "test-hash",
                "Test User",
                "USER",
                "ACTIVE");

        // Create a job
        jdbcTemplate.update("""
                INSERT INTO jobs
                    (id, user_id, name, status, schedule_type, schedule_value,
                     timezone, http_method, url, headers, query_params,
                     max_retries, initial_retry_delay_ms, max_retry_delay_ms,
                     created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, '{}'::jsonb, '{}'::jsonb,
                        ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                jobId,
                userId,
                "Concurrent Claim Test",
                "ACTIVE",
                "ONE_TIME",
                "2026-10-01T10:00:00",
                "UTC",
                "GET",
                "https://example.com",
                0,
                2000L,
                30000L);

        // Create one pending execution
        jdbcTemplate.update("""
                INSERT INTO executions
                    (id, job_id, scheduled_at, status, max_retries,
                     initial_retry_delay_ms, max_retry_delay_ms,
                     created_at, updated_at)
                VALUES (?, ?, CURRENT_TIMESTAMP, 'PENDING', ?, ?, ?,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                executionId,
                jobId,
                0,
                2000L,
                30000L);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> first = executor.submit(() -> {
                ready.countDown();
                start.await();
                return executionClaimService.claimExecution(executionId);
            });

            Future<Boolean> second = executor.submit(() -> {
                ready.countDown();
                start.await();
                return executionClaimService.claimExecution(executionId);
            });

            // Ensure both threads are ready before racing them.
            assertTrue(ready.await(5, java.util.concurrent.TimeUnit.SECONDS));
            start.countDown();

            boolean firstResult = first.get();
            boolean secondResult = second.get();

            // Exactly one thread must claim the execution.
            assertNotEquals(firstResult, secondResult);
        }

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM executions WHERE id = ?",
                String.class,
                executionId);

        assertEquals("RUNNING", status);

        Instant startedAt = jdbcTemplate.queryForObject(
                "SELECT started_at FROM executions WHERE id = ?",
                (rs, rowNum) -> rs.getTimestamp("started_at").toInstant(),
                executionId);

        assertNotNull(startedAt);
    }
}
