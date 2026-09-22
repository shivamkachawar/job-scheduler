package com.shivam.job_scheduler.job.service;

import com.shivam.job_scheduler.job.dto.CreateJobRequest;
import com.shivam.job_scheduler.job.entity.Job;

import java.util.UUID;

public interface JobService {

    Job createJob(UUID userId, CreateJobRequest request);
}