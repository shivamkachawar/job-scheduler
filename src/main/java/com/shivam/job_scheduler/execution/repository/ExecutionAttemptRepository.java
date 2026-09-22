package com.shivam.job_scheduler.execution.repository;

import com.shivam.job_scheduler.execution.entity.ExecutionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExecutionAttemptRepository extends JpaRepository<ExecutionAttempt, UUID> {
}