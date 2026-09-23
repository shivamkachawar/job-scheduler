package com.shivam.job_scheduler.scheduling;

import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.job.entity.JobStatus;
import com.shivam.job_scheduler.job.repository.JobRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class JobScheduler {
    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);
    private final JobRepository jobRepository;
    private final JobScheduleQueue scheduleQueue;
    private Instant schedulerStartedAt;
    private final ScheduledJobProcessor scheduledJobProcessor;

    public JobScheduler(JobRepository jobRepository, JobScheduleQueue scheduleQueue,
            ScheduledJobProcessor scheduledJobProcessor) {
        this.jobRepository = jobRepository;
        this.scheduleQueue = scheduleQueue;
        this.scheduledJobProcessor = scheduledJobProcessor;
    }

    @PostConstruct
    public void start() {
        schedulerStartedAt = Instant.now();
        loadJobs();
        Thread schedulerThread = new Thread(this::runScheduler, "job-scheduler");
        schedulerThread.start();
    }

    private void loadJobs() {
        List<Job> jobs = jobRepository.findByStatusAndNextRunAtIsNotNull(JobStatus.ACTIVE);
        for (Job job : jobs) {
            scheduleQueue.add(job);
        }
        log.info("Loaded {} active jobs into scheduler", jobs.size());
    }

    public void runScheduler() {
        log.info("Job scheduler started");

        while (true) {
            try {
                Job job = scheduleQueue.waitUntilNextJobIsDue();

                log.info("Job is due : id={}, name={}, nextRunAt={}", job.getId(), job.getName(), job.getNextRunAt());

                scheduledJobProcessor.process(job, schedulerStartedAt);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Job scheduler interrupted");
                break;
            }
        }
    }

}
