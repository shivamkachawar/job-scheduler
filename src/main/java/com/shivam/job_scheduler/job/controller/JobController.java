package com.shivam.job_scheduler.job.controller;

import com.shivam.job_scheduler.job.dto.CreateJobRequest;
import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public Job createJob(
            @RequestParam UUID userId,
            @Valid @RequestBody CreateJobRequest request) {
        return jobService.createJob(userId, request);
    }

    @PatchMapping("/{jobId}/pause")
    public void pauseJob(@PathVariable UUID jobId) {
        jobService.pauseJob(jobId);
    }

    @PatchMapping("/{jobId}/resume")
    public void resumeJob(@PathVariable UUID jobId) {
        jobService.resumeJob(jobId);
    }

    @DeleteMapping("/{jobId}")
    public void deleteJob(@PathVariable UUID jobId) {
        jobService.deleteJob(jobId);
    }
}