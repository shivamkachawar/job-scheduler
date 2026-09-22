package com.shivam.job_scheduler.execution.repository;

import com.shivam.job_scheduler.execution.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
}