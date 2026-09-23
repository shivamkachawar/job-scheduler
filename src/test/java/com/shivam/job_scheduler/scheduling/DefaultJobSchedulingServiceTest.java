package com.shivam.job_scheduler.scheduling;

import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.job.entity.ScheduleType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultJobSchedulingServiceTest {

    private final ScheduleCalculator scheduleCalculator = new DefaultScheduleCalculator();

    private final DefaultJobSchedulingService service = new DefaultJobSchedulingService(scheduleCalculator);

    @Test
    void shouldAdvanceIntervalSchedule() {
        Job job = new Job();

        job.setScheduleType(ScheduleType.INTERVAL);
        job.setScheduleValue("PT5M");
        job.setTimezone("Asia/Kolkata");

        Instant currentRun = Instant.parse("2026-09-22T12:49:36Z");
        job.setNextRunAt(currentRun);

        service.advanceNextRun(job);

        assertEquals(
                Instant.parse("2026-09-22T12:54:36Z"),
                job.getNextRunAt());
    }

    @Test
    void shouldAdvanceCronSchedule() {
        Job job = new Job();

        job.setScheduleType(ScheduleType.CRON);
        job.setScheduleValue("0 0 10 * * *");
        job.setTimezone("Asia/Kolkata");

        Instant currentRun = Instant.parse("2026-09-22T04:30:00Z");
        job.setNextRunAt(currentRun);

        service.advanceNextRun(job);

        assertEquals(
                Instant.parse("2026-09-23T04:30:00Z"),
                job.getNextRunAt());
    }

    @Test
    void shouldClearNextRunForOneTimeSchedule() {
        Job job = new Job();

        job.setScheduleType(ScheduleType.ONE_TIME);
        job.setScheduleValue("2026-09-22T10:00:00");
        job.setTimezone("Asia/Kolkata");

        Instant currentRun = Instant.parse("2026-09-22T04:30:00Z");
        job.setNextRunAt(currentRun);

        service.advanceNextRun(job);

        assertNull(job.getNextRunAt());
    }

    @Test
    void shouldFindNextFutureIntervalRun() {
        Job job = new Job();

        job.setScheduleType(ScheduleType.INTERVAL);
        job.setScheduleValue("PT5M");
        job.setTimezone("Asia/Kolkata");

        job.setNextRunAt(
                Instant.parse("2026-09-22T12:49:36Z"));

        Instant now = Instant.parse("2026-09-23T05:00:00Z");

        Instant result = service.calculateNextFutureRun(job, now);

        assertTrue(result.isAfter(now));
    }

    @Test
    void shouldFindExactNextFutureIntervalRun() {
        Job job = new Job();

        job.setScheduleType(ScheduleType.INTERVAL);
        job.setScheduleValue("PT5M");
        job.setTimezone("Asia/Kolkata");

        job.setNextRunAt(
                Instant.parse("2026-09-23T04:19:36Z"));

        Instant now = Instant.parse("2026-09-23T05:00:00Z");

        Instant result = service.calculateNextFutureRun(job, now);

        assertEquals(
                Instant.parse("2026-09-23T05:04:36Z"),
                result);
    }
}