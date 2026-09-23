package com.shivam.job_scheduler.scheduling;

import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.job.entity.ScheduleType;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DefaultJobSchedulingService implements JobSchedulingService {

    private final ScheduleCalculator scheduleCalculator;

    public DefaultJobSchedulingService(ScheduleCalculator scheduleCalculator) {
        this.scheduleCalculator = scheduleCalculator;
    }

    @Override
    public void advanceNextRun(Job job) {
        if (job.getNextRunAt() == null) {
            return;
        }

        if (job.getScheduleType() == ScheduleType.ONE_TIME) {
            job.setNextRunAt(null);
            return;
        }

        Instant nextRun = scheduleCalculator.calculateNext(
                job.getScheduleType(),
                job.getScheduleValue(),
                job.getTimezone(),
                job.getNextRunAt());

        job.setNextRunAt(nextRun);
    }

    @Override
    public Instant calculateNextFutureRun(Job job, Instant now) {
        if (job.getScheduleType() == ScheduleType.ONE_TIME) {
            return null;
        }
        Instant nextRun = job.getNextRunAt();
        if (nextRun == null)
            return null;

        while (!nextRun.isAfter(now)) {
            nextRun = scheduleCalculator.calculateNext(job.getScheduleType(), job.getScheduleValue(), job.getTimezone(),
                    nextRun);
        }
        return nextRun;
    }
}
