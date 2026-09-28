package com.shivam.job_scheduler.scheduling;

import com.shivam.job_scheduler.execution.service.ExecutionService;
import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.job.entity.JobStatus;
import com.shivam.job_scheduler.job.repository.JobRepository;
import com.shivam.job_scheduler.outbox.service.OutboxService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import com.shivam.job_scheduler.execution.entity.Execution;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledJobProcessorTest {

    @Mock
    private ExecutionService executionService;

    @Mock
    private JobSchedulingService jobSchedulingService;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobScheduleQueue scheduleQueue;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private ScheduledJobProcessor processor;

    @Test
    void shouldMarkOverdueJobAsMissedWhenSchedulerStarts() {
        UUID jobId = UUID.randomUUID();

        Instant scheduledAt = Instant.parse("2026-09-28T09:00:00Z");

        Instant schedulerStartedAt = Instant.parse("2026-09-28T10:00:00Z");

        Instant nextRun = Instant.parse("2026-09-29T09:00:00Z");

        Job job = mock(Job.class);

        when(job.getId()).thenReturn(jobId);
        when(job.getStatus()).thenReturn(JobStatus.ACTIVE);
        when(job.getNextRunAt()).thenReturn(scheduledAt);

        when(jobRepository.findById(jobId))
                .thenReturn(Optional.of(job));

        when(jobSchedulingService.calculateNextFutureRun(
                eq(job), any(Instant.class)))
                .thenReturn(nextRun);

        processor.process(job, schedulerStartedAt);

        // The overdue occurrence is marked MISSED.
        verify(executionService)
                .createMissedExecution(job, "SCHEDULER_UNAVAILABLE");

        // It must not be executed or sent to Kafka.
        verify(executionService, never())
                .createExecution(job);

        verify(outboxService, never())
                .createExecutionRequestedEvent(
                        any(UUID.class), any(Instant.class));

        // The next occurrence is persisted and queued.
        verify(job).setNextRunAt(nextRun);
        verify(jobRepository).save(job);
        verify(scheduleQueue).add(job);
    }

    @Test
    void shouldCreateExecutionWhenJobIsDueAfterSchedulerStarts() {
        UUID jobId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();

        Instant schedulerStartedAt = Instant.parse("2026-09-28T09:00:00Z");

        Instant scheduledAt = Instant.parse("2026-09-28T10:00:00Z");

        Instant nextRun = Instant.parse("2026-09-29T10:00:00Z");

        Job job = mock(Job.class);
        Execution execution = mock(Execution.class);

        when(job.getId()).thenReturn(jobId);
        when(job.getStatus()).thenReturn(JobStatus.ACTIVE);
        when(job.getNextRunAt()).thenReturn(scheduledAt);

        when(jobRepository.findById(jobId))
                .thenReturn(Optional.of(job));

        when(executionService.createExecution(job))
                .thenReturn(execution);

        when(execution.getId()).thenReturn(executionId);
        when(execution.getScheduledAt()).thenReturn(scheduledAt);

        when(jobSchedulingService.calculateNextFutureRun(
                eq(job), any(Instant.class)))
                .thenReturn(nextRun);

        processor.process(job, schedulerStartedAt);

        // Create the execution for this scheduled occurrence.
        verify(executionService).createExecution(job);

        // Create an outbox event for the execution.
        verify(outboxService)
                .createExecutionRequestedEvent(
                        executionId, scheduledAt);

        // This occurrence must not be marked as missed.
        verify(executionService, never())
                .createMissedExecution(
                        any(Job.class), any(String.class));

        // Persist and queue the next occurrence.
        verify(job).setNextRunAt(nextRun);
        verify(jobRepository).save(job);
        verify(scheduleQueue).add(job);
    }

    @Test
    void shouldExecuteJobWhenScheduledAtExactlyMatchesSchedulerStart() {
        UUID jobId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();

        Instant schedulerStartedAt = Instant.parse("2026-09-28T10:00:00Z");

        // Exactly the same Instant as schedulerStartedAt
        Instant scheduledAt = schedulerStartedAt;

        Instant nextRun = Instant.parse("2026-09-29T10:00:00Z");

        Job job = mock(Job.class);
        Execution execution = mock(Execution.class);

        when(job.getId()).thenReturn(jobId);
        when(job.getStatus()).thenReturn(JobStatus.ACTIVE);
        when(job.getNextRunAt()).thenReturn(scheduledAt);

        when(jobRepository.findById(jobId))
                .thenReturn(Optional.of(job));

        when(executionService.createExecution(job))
                .thenReturn(execution);

        when(execution.getId()).thenReturn(executionId);
        when(execution.getScheduledAt()).thenReturn(scheduledAt);

        when(jobSchedulingService.calculateNextFutureRun(
                eq(job), any(Instant.class)))
                .thenReturn(nextRun);

        processor.process(job, schedulerStartedAt);

        // Equality is NOT considered overdue.
        verify(executionService).createExecution(job);

        verify(outboxService)
                .createExecutionRequestedEvent(
                        executionId, scheduledAt);

        verify(executionService, never())
                .createMissedExecution(
                        any(Job.class), any(String.class));

        verify(job).setNextRunAt(nextRun);
        verify(jobRepository).save(job);
        verify(scheduleQueue).add(job);
    }
}