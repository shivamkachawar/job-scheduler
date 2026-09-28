package com.shivam.job_scheduler.scheduling;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.shivam.job_scheduler.job.entity.Job;

class JobScheduleQueueTest {

    @Test
    void shouldReturnJobsInScheduledTimeOrder() {
        JobScheduleQueue queue = new JobScheduleQueue();

        Job laterJob = mock(Job.class);
        Job earlierJob = mock(Job.class);
        Job middleJob = mock(Job.class);

        when(laterJob.getNextRunAt())
                .thenReturn(Instant.parse("2026-09-30T10:00:00Z"));

        when(earlierJob.getNextRunAt())
                .thenReturn(Instant.parse("2026-09-28T10:00:00Z"));

        when(middleJob.getNextRunAt())
                .thenReturn(Instant.parse("2026-09-29T10:00:00Z"));

        // Deliberately insert them out of order.
        queue.add(laterJob);
        queue.add(earlierJob);
        queue.add(middleJob);

        // They should come out in chronological order.
        assertSame(earlierJob, queue.poll());
        assertSame(middleJob, queue.poll());
        assertSame(laterJob, queue.poll());
    }

    @Test
    void shouldNotReturnRemovedJob() {
        JobScheduleQueue queue = new JobScheduleQueue();

        Job removedJob = mock(Job.class);
        Job remainingJob = mock(Job.class);

        when(removedJob.getNextRunAt())
                .thenReturn(Instant.parse("2026-09-28T10:00:00Z"));

        when(remainingJob.getNextRunAt())
                .thenReturn(Instant.parse("2026-09-29T10:00:00Z"));

        when(removedJob.getId())
                .thenReturn(java.util.UUID.randomUUID());

        queue.add(removedJob);
        queue.add(remainingJob);
        UUID removedJobId = UUID.randomUUID();
        UUID remainingJobId = UUID.randomUUID();

        when(removedJob.getId()).thenReturn(removedJobId);
        when(remainingJob.getId()).thenReturn(remainingJobId);
        queue.remove(removedJobId);

        assertSame(remainingJob, queue.poll());
        org.junit.jupiter.api.Assertions.assertNull(queue.poll());
    }

    @Test
    void shouldWakeWaitingThreadWhenEarlierJobIsAdded() throws Exception {
        JobScheduleQueue queue = new JobScheduleQueue();

        Job laterJob = mock(Job.class);
        Job earlierJob = mock(Job.class);

        when(laterJob.getNextRunAt())
                .thenReturn(Instant.now().plusSeconds(60));

        // This job is already due when added.
        when(earlierJob.getNextRunAt())
                .thenReturn(Instant.now().minusSeconds(1));

        queue.add(laterJob);

        FutureTask<Job> task = new FutureTask<>(queue::waitUntilNextJobIsDue);

        Thread waitingThread = new Thread(task);
        waitingThread.start();

        try {
            // Wait until the worker is waiting on the queue's Condition.
            long deadline = System.nanoTime()
                    + TimeUnit.SECONDS.toNanos(5);

            while (waitingThread.getState() != Thread.State.WAITING
                    && waitingThread.getState() != Thread.State.TIMED_WAITING
                    && System.nanoTime() < deadline) {
                Thread.yield();
            }
            assertTrue(
                    waitingThread.getState() == Thread.State.WAITING
                            || waitingThread.getState() == Thread.State.TIMED_WAITING,
                    "Scheduler thread did not enter a waiting state");

            // Adding this due job should signal and wake the thread.
            queue.add(earlierJob);

            assertSame(
                    earlierJob,
                    task.get(2, TimeUnit.SECONDS));
        } finally {
            waitingThread.interrupt();
            waitingThread.join(2_000);
        }
    }
}