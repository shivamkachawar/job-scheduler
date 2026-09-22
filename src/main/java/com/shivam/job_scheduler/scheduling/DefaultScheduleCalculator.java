package com.shivam.job_scheduler.scheduling;

import com.shivam.job_scheduler.job.entity.ScheduleType;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class DefaultScheduleCalculator implements ScheduleCalculator {

    @Override
    public Instant calculateNext(
            ScheduleType scheduleType,
            String scheduleValue,
            String timezone,
            Instant referenceTime) {
        if (scheduleType == null) {
            throw new ScheduleCalculationException(
                    "Schedule type must not be null");
        }

        if (scheduleValue == null || scheduleValue.isBlank()) {
            throw new ScheduleCalculationException(
                    "Schedule value must not be blank");
        }

        if (timezone == null || timezone.isBlank()) {
            throw new ScheduleCalculationException(
                    "Timezone must not be blank");
        }

        if (referenceTime == null) {
            throw new ScheduleCalculationException(
                    "Reference time must not be null");
        }

        return switch (scheduleType) {
            case ONE_TIME -> calculateOneTime(scheduleValue, timezone);
            case INTERVAL -> calculateInterval(scheduleValue, referenceTime);
            case CRON -> calculateCron(scheduleValue, timezone, referenceTime);
        };
    }

    private Instant calculateOneTime(
            String scheduleValue,
            String timezone) {
        try {
            ZoneId zoneId = ZoneId.of(timezone);

            return java.time.LocalDateTime
                    .parse(scheduleValue)
                    .atZone(zoneId)
                    .toInstant();

        } catch (Exception e) {
            throw new ScheduleCalculationException(
                    "Invalid ONE_TIME schedule: " + scheduleValue,
                    e);
        }
    }

    private Instant calculateInterval(
            String scheduleValue,
            Instant referenceTime) {
        try {
            Duration interval = Duration.parse(scheduleValue);

            if (interval.isZero() || interval.isNegative()) {
                throw new ScheduleCalculationException(
                        "INTERVAL must be greater than zero");
            }

            return referenceTime.plus(interval);

        } catch (ScheduleCalculationException e) {
            throw e;

        } catch (Exception e) {
            throw new ScheduleCalculationException(
                    "Invalid INTERVAL schedule: " + scheduleValue,
                    e);
        }
    }

    private Instant calculateCron(
            String scheduleValue,
            String timezone,
            Instant referenceTime) {
        try {
            CronExpression cronExpression = CronExpression.parse(scheduleValue);

            ZoneId zoneId = ZoneId.of(timezone);

            ZonedDateTime reference = referenceTime.atZone(zoneId);

            ZonedDateTime next = cronExpression.next(reference);

            if (next == null) {
                throw new ScheduleCalculationException(
                        "No next occurrence exists for CRON: "
                                + scheduleValue);
            }

            return next.toInstant();

        } catch (ScheduleCalculationException e) {
            throw e;

        } catch (Exception e) {
            throw new ScheduleCalculationException(
                    "Invalid CRON schedule: " + scheduleValue,
                    e);
        }
    }
}