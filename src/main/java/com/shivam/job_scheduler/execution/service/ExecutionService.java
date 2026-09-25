package com.shivam.job_scheduler.execution.service;

import java.util.UUID;

import com.shivam.job_scheduler.execution.entity.Execution;
import com.shivam.job_scheduler.execution.entity.ExecutionStatus;
import com.shivam.job_scheduler.job.entity.Job;

public interface ExecutionService {

    Execution createExecution(Job job);

    Execution createMissedExecution(Job job, String reason);

    void completeExecution(Execution execution, ExecutionStatus status);

    boolean markMissedAndExpire(UUID executionId);

}