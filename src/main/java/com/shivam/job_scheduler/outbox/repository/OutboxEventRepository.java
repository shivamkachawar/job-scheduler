package com.shivam.job_scheduler.outbox.repository;

import com.shivam.job_scheduler.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByPublishedAtIsNullAndExpiredAtIsNullOrderByCreatedAtAsc();

    @Modifying
    @Query("""
            UPDATE OutboxEvent e
            SET e.expiredAt = :expiredAt
            WHERE e.aggregateId = :executionId
              AND e.publishedAt IS NULL
              AND e.expiredAt IS NULL
            """)
    int expirePendingEvent(
            @Param("executionId") UUID executionId,
            @Param("expiredAt") Instant expiredAt);
}