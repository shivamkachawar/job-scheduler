package com.shivam.job_scheduler.execution.controller;

import com.shivam.job_scheduler.execution.dto.ExecutionDetailsResponse;
import com.shivam.job_scheduler.execution.service.ExecutionDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/executions")
public class ExecutionDetailsController {

    private final ExecutionDetailsService executionDetailsService;

    public ExecutionDetailsController(
            ExecutionDetailsService executionDetailsService) {
        this.executionDetailsService = executionDetailsService;
    }

    @GetMapping("/{executionId}")
    public ExecutionDetailsResponse getExecutionDetails(
            @PathVariable UUID executionId) {

        return executionDetailsService.getExecutionDetails(executionId);
    }
}