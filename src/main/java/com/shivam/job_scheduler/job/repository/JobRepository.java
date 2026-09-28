package com.shivam.job_scheduler.job.repository;

import com.shivam.job_scheduler.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import com.shivam.job_scheduler.job.entity.JobStatus;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
    List<Job> findByStatusAndNextRunAtIsNotNull(JobStatus status);

    Page<Job> findByUser_IdOrderByCreatedAtDescIdDesc(
            UUID userId,
            Pageable pageable);
}