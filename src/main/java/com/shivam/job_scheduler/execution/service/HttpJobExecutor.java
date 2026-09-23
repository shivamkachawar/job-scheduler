package com.shivam.job_scheduler.execution.service;

import com.shivam.job_scheduler.job.entity.Job;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class HttpJobExecutor {

    private final RestClient restClient;

    public HttpJobExecutor(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public HttpExecutionResult execute(Job job) {

        long start = System.currentTimeMillis();

        try {

            HttpExecutionResult result = restClient
                    .method(
                            org.springframework.http.HttpMethod.valueOf(
                                    job.getHttpMethod().name()))
                    .uri(job.getUrl())
                    .headers(headers -> {
                        job.getHeaders().forEach(headers::add);
                    })
                    .exchange((request, response) -> {

                        String responseBody = response.bodyTo(String.class);

                        return new HttpExecutionResult(
                                response.getStatusCode().is2xxSuccessful(),
                                response.getStatusCode().value(),
                                responseBody,
                                0);
                    });

            long duration = System.currentTimeMillis() - start;

            return new HttpExecutionResult(
                    result.success(),
                    result.httpStatusCode(),
                    result.responseBody(),
                    duration);

        } catch (Exception e) {

            long duration = System.currentTimeMillis() - start;

            e.printStackTrace();

            return new HttpExecutionResult(
                    false,
                    null,
                    e.getMessage(),
                    duration);
        }
    }
}