package com.shivam.job_scheduler.scheduling;

import com.shivam.job_scheduler.job.entity.ScheduleType;

import java.time.Instant;

public interface ScheduleCalculator {

    Instant calculateNext(
            ScheduleType scheduleType,
            String scheduleValue,
            String timezone,
            Instant referenceTime);
}