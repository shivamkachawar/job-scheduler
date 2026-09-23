package com.shivam.job_scheduler.execution.controller;

import com.shivam.job_scheduler.execution.service.ExecutionClaimService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    private final ExecutionClaimService executionClaimService;

    public ExecutionController(ExecutionClaimService executionClaimService) {
        this.executionClaimService = executionClaimService;
    }

    @PostMapping("/{executionId}/claim")
    public boolean claimExecution(@PathVariable UUID executionId) {
        return executionClaimService.claimExecution(executionId);
    }
}