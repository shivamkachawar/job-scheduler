package com.shivam.job_scheduler.execution.service;

import com.shivam.job_scheduler.execution.entity.Execution;
import com.shivam.job_scheduler.job.entity.Job;

public interface ExecutionService {

    Execution createExecution(Job job);

    Execution createMissedExecution(Job job, String reason);
}