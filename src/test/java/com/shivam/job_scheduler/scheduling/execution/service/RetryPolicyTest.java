package com.shivam.job_scheduler.scheduling.execution.service;

import com.shivam.job_scheduler.execution.entity.AttemptErrorType;
import com.shivam.job_scheduler.execution.service.RetryPolicy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

        private final RetryPolicy retryPolicy = new RetryPolicy();

        @Test
        void shouldRetry5xx() {
                assertTrue(
                                retryPolicy.shouldRetry(
                                                AttemptErrorType.HTTP_ERROR,
                                                500,
                                                1,
                                                3));
        }

        @Test
        void shouldNotRetry4xx() {
                assertFalse(
                                retryPolicy.shouldRetry(
                                                AttemptErrorType.HTTP_ERROR,
                                                404,
                                                1,
                                                3));
        }

        @Test
        void shouldRetryTimeout() {
                assertTrue(
                                retryPolicy.shouldRetry(
                                                AttemptErrorType.TIMEOUT,
                                                null,
                                                1,
                                                3));
        }

        @Test
        void shouldRetryConnectionError() {
                assertTrue(
                                retryPolicy.shouldRetry(
                                                AttemptErrorType.CONNECTION_ERROR,
                                                null,
                                                1,
                                                3));
        }

        @Test
        void shouldNotRetryWhenMaxRetriesReached() {
                assertFalse(
                                retryPolicy.shouldRetry(
                                                AttemptErrorType.HTTP_ERROR,
                                                500,
                                                4,
                                                3));
        }

        @Test
        void shouldCalculateExponentialBackoff() {
                assertEquals(
                                2000,
                                retryPolicy.calculateDelay(2000, 30000, 1));

                assertEquals(
                                4000,
                                retryPolicy.calculateDelay(2000, 30000, 2));

                assertEquals(
                                8000,
                                retryPolicy.calculateDelay(2000, 30000, 3));
        }

        @Test
        void shouldRespectMaximumDelay() {
                assertEquals(
                                30000,
                                retryPolicy.calculateDelay(2000, 30000, 5));
        }
}