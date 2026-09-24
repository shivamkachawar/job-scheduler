package com.shivam.job_scheduler.execution.service;

import com.shivam.job_scheduler.execution.entity.AttemptErrorType;
import com.shivam.job_scheduler.job.entity.Job;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

@Service
public class HttpJobExecutor {

    private final HttpClient httpClient;

    public HttpJobExecutor(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpExecutionResult execute(Job job) {

        long start = System.currentTimeMillis();

        try {

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(job.getUrl()))
                    .method(
                            job.getHttpMethod().name(),
                            HttpRequest.BodyPublishers.noBody());
            Integer timeoutMs = job.getTimeoutMs();
            if (timeoutMs != null) {
                requestBuilder.timeout(Duration.ofMillis(timeoutMs));
            }

            job.getHeaders().forEach(requestBuilder::header);

            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            long duration = System.currentTimeMillis() - start;

            int statusCode = response.statusCode();
            boolean success = statusCode >= 200 && statusCode < 300;

            return new HttpExecutionResult(
                    success,
                    statusCode,
                    response.body(),
                    duration,
                    success ? null : AttemptErrorType.HTTP_ERROR,
                    success ? null : "HTTP error with status code: " + statusCode);

        } catch (HttpConnectTimeoutException e) {

            long duration = System.currentTimeMillis() - start;

            return new HttpExecutionResult(
                    false,
                    null,
                    null,
                    duration,
                    AttemptErrorType.TIMEOUT,
                    e.getMessage());

        } catch (HttpTimeoutException e) {

            long duration = System.currentTimeMillis() - start;

            return new HttpExecutionResult(
                    false,
                    null,
                    null,
                    duration,
                    AttemptErrorType.TIMEOUT,
                    e.getMessage());

        } catch (IOException e) {

            long duration = System.currentTimeMillis() - start;

            return new HttpExecutionResult(
                    false,
                    null,
                    null,
                    duration,
                    AttemptErrorType.CONNECTION_ERROR,
                    e.getMessage());

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            long duration = System.currentTimeMillis() - start;

            return new HttpExecutionResult(
                    false,
                    null,
                    null,
                    duration,
                    AttemptErrorType.UNKNOWN,
                    e.getMessage());
        }
    }
}