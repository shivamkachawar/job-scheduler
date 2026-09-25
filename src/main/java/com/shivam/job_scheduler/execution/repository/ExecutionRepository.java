package com.shivam.job_scheduler.execution.repository;

import com.shivam.job_scheduler.execution.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.shivam.job_scheduler.execution.entity.ExecutionStatus;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
        @Modifying
        @Query("""
                            UPDATE Execution e
                            SET e.status = com.shivam.job_scheduler.execution.entity.ExecutionStatus.RUNNING,
                                e.startedAt = :startedAt
                            WHERE e.id = :executionId
                              AND e.status = com.shivam.job_scheduler.execution.entity.ExecutionStatus.PENDING
                              AND e.scheduledAt >= :deadlineCutoff
                        """)
        int claimExecution(
                        @Param("executionId") UUID executionId,
                        @Param("startedAt") Instant startedAt,
                        @Param("deadlineCutoff") Instant deadlineCutoff);

        Optional<Execution> findFirstByStatusOrderByCreatedAtAsc(ExecutionStatus status);

        @Query("""
                        SELECT e
                        FROM Execution e
                        JOIN FETCH e.job
                        WHERE e.id = :executionId
                        """)
        Optional<Execution> findByIdWithJob(
                        @Param("executionId") UUID executionId);

        @Modifying
        @Query("""
                        UPDATE Execution e
                        SET e.status = :newStatus,
                            e.reason = :reason,
                            e.completedAt = :completedAt,
                            e.updatedAt = :completedAt
                        WHERE e.id = :executionId
                          AND e.status = :currentStatus
                          AND e.scheduledAt < :deadlineCutoff
                        """)
        int markMissedIfPending(
                        @Param("executionId") UUID executionId,
                        @Param("currentStatus") ExecutionStatus currentStatus,
                        @Param("newStatus") ExecutionStatus newStatus,
                        @Param("reason") String reason,
                        @Param("completedAt") Instant completedAt,
                        @Param("deadlineCutoff") Instant deadlineCutoff);
}