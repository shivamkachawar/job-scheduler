package com.shivam.job_scheduler.execution.controller;

import com.shivam.job_scheduler.execution.dto.ExecutionHistoryResponse;
import com.shivam.job_scheduler.execution.service.ExecutionHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs/{jobId}/executions")
public class ExecutionHistoryController {

    private final ExecutionHistoryService executionHistoryService;

    public ExecutionHistoryController(
            ExecutionHistoryService executionHistoryService) {
        this.executionHistoryService = executionHistoryService;
    }

    @GetMapping
    public Page<ExecutionHistoryResponse> getExecutionHistory(
            @PathVariable UUID jobId,
            @PageableDefault(size = 10) Pageable pageable) {

        return executionHistoryService.getExecutionHistory(
                jobId,
                pageable);
    }
}