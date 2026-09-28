package com.shivam.job_scheduler.execution.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExecutionNotFoundException extends RuntimeException {

    public ExecutionNotFoundException(UUID executionId) {
        super("Execution not found with ID: " + executionId);
    }
}