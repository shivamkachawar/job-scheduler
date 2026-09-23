package com.shivam.job_scheduler.scheduling;

import com.shivam.job_scheduler.job.entity.Job;
import java.time.Instant;

public interface JobSchedulingService {

    void advanceNextRun(Job job);

    Instant calculateNextFutureRun(Job job, Instant now);
}