package com.shivam.job_scheduler.job.repository;

import com.shivam.job_scheduler.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import com.shivam.job_scheduler.job.entity.JobStatus;

import java.util.List;

import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
    List<Job> findByStatusAndNextRunAtIsNotNull(JobStatus status);
}