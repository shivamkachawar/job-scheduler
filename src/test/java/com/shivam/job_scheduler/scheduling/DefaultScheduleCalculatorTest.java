package com.shivam.job_scheduler.scheduling;

import com.shivam.job_scheduler.job.entity.ScheduleType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultScheduleCalculatorTest {

    private final ScheduleCalculator calculator = new DefaultScheduleCalculator();

    @Test
    void shouldCalculateInterval() {

        Instant reference = Instant.parse("2026-09-22T10:00:00Z");

        Instant result = calculator.calculateNext(
                ScheduleType.INTERVAL,
                "PT5M",
                "UTC",
                reference);

        assertEquals(
                Instant.parse("2026-09-22T10:05:00Z"),
                result);
    }

    @Test
    void shouldCalculateOneTime() {

        Instant reference = Instant.parse("2026-09-22T10:00:00Z");

        Instant result = calculator.calculateNext(
                ScheduleType.ONE_TIME,
                "2026-10-01T10:00:00",
                "Asia/Kolkata",
                reference);

        assertEquals(
                Instant.parse("2026-10-01T04:30:00Z"),
                result);
    }

    @Test
    void shouldCalculateCron() {

        Instant reference = Instant.parse("2026-09-22T04:00:00Z");

        Instant result = calculator.calculateNext(
                ScheduleType.CRON,
                "0 0 10 * * *",
                "Asia/Kolkata",
                reference);

        assertEquals(
                Instant.parse("2026-09-22T04:30:00Z"),
                result);
    }

    @Test
    void shouldRejectInvalidInterval() {

        Instant reference = Instant.parse("2026-09-22T10:00:00Z");

        assertThrows(
                ScheduleCalculationException.class,
                () -> calculator.calculateNext(
                        ScheduleType.INTERVAL,
                        "INVALID",
                        "UTC",
                        reference));
    }

    @Test
    void shouldRejectInvalidTimezone() {

        Instant reference = Instant.parse("2026-09-22T10:00:00Z");

        assertThrows(
                ScheduleCalculationException.class,
                () -> calculator.calculateNext(
                        ScheduleType.CRON,
                        "0 10 * * *",
                        "INVALID/TIMEZONE",
                        reference));
    }
}