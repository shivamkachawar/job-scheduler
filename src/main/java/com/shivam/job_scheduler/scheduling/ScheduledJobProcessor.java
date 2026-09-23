package com.shivam.job_scheduler.scheduling;

import com.shivam.job_scheduler.execution.service.ExecutionService;
import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.job.entity.JobStatus;
import com.shivam.job_scheduler.job.repository.JobRepository;

import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.time.Instant;

import org.springframework.stereotype.Service;

@Service
public class ScheduledJobProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobProcessor.class);

    private final ExecutionService executionService;
    private final JobSchedulingService jobSchedulingService;
    private final JobRepository jobRepository;
    private final JobScheduleQueue scheduleQueue;

    public ScheduledJobProcessor(ExecutionService executionService, JobSchedulingService jobSchedulingService,
            JobRepository jobRepository, JobScheduleQueue scheduleQueue) {
        this.executionService = executionService;
        this.jobSchedulingService = jobSchedulingService;
        this.jobRepository = jobRepository;
        this.scheduleQueue = scheduleQueue;
    }

    @Transactional
    public void process(Job job, Instant schedulerStartedAt) {

        Instant now = Instant.now();

        Job currentJob = jobRepository.findById(job.getId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (currentJob.getStatus() == JobStatus.PAUSED) {
            log.info("Skipping paused job: id={}", currentJob.getId());
            return;
        }

        Instant scheduledAt = currentJob.getNextRunAt();

        if (scheduledAt.isBefore(schedulerStartedAt)) {
            executionService.createMissedExecution(
                    currentJob,
                    "SCHEDULER_UNAVAILABLE");
        } else {
            executionService.createExecution(currentJob);
        }

        Instant nextRun = jobSchedulingService.calculateNextFutureRun(currentJob, now);

        currentJob.setNextRunAt(nextRun);

        jobRepository.save(currentJob);

        if (nextRun != null) {
            scheduleQueue.add(currentJob);
        }
    }
}