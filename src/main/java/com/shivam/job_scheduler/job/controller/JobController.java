package com.shivam.job_scheduler.job.controller;

import com.shivam.job_scheduler.job.dto.CreateJobRequest;
import com.shivam.job_scheduler.job.dto.JobDetailsResponse;
import com.shivam.job_scheduler.job.dto.JobListResponse;
import com.shivam.job_scheduler.job.entity.Job;
import com.shivam.job_scheduler.job.service.JobDetailsService;
import com.shivam.job_scheduler.job.service.JobListService;
import com.shivam.job_scheduler.job.service.JobService;
import jakarta.validation.Valid;

import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final JobListService jobListService;
    private final JobDetailsService jobDetailsService;

    public JobController(JobService jobService, JobListService jobListService, JobDetailsService jobDetailsService) {
        this.jobService = jobService;
        this.jobListService = jobListService;
        this.jobDetailsService = jobDetailsService;
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

    @GetMapping
    public Page<JobListResponse> getJobs(
            @RequestParam UUID userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return jobListService.getJobs(userId, pageable);
    }

    @GetMapping("/{jobId}")
    public JobDetailsResponse getJobDetails(
            @PathVariable UUID jobId) {
        return jobDetailsService.getJobDetails(jobId);
    }

}