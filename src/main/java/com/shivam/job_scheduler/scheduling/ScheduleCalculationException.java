package com.shivam.job_scheduler.scheduling;

public class ScheduleCalculationException extends RuntimeException {

    public ScheduleCalculationException(String message) {
        super(message);
    }

    public ScheduleCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}