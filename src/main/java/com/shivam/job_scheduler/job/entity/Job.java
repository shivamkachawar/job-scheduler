package com.shivam.job_scheduler.job.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.shivam.job_scheduler.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 20)
    private ScheduleType scheduleType;

    @Column(name = "schedule_value", nullable = false)
    private String scheduleValue;

    @Column(nullable = false, length = 100)
    private String timezone;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false, length = 10)
    private HttpMethod httpMethod;

    @Column(nullable = false)
    private String url;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, String> headers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "query_params", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> queryParams;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode body;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "timeout_ms")
    private Integer timeoutMs;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "initial_retry_delay_ms", nullable = false)
    private Long initialRetryDelayMs;

    @Column(name = "max_retry_delay_ms", nullable = false)
    private Long maxRetryDelayMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Job() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public JobStatus getStatus() {
        return status;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public String getScheduleValue() {
        return scheduleValue;
    }

    public String getTimezone() {
        return timezone;
    }

    public Instant getNextRunAt() {
        return nextRunAt;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public JsonNode getBody() {
        return body;
    }

    public String getContentType() {
        return contentType;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public Long getInitialRetryDelayMs() {
        return initialRetryDelayMs;
    }

    public Long getMaxRetryDelayMs() {
        return maxRetryDelayMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public void setScheduleValue(String scheduleValue) {
        this.scheduleValue = scheduleValue;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setNextRunAt(Instant nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setInitialRetryDelayMs(Long initialRetryDelayMs) {
        this.initialRetryDelayMs = initialRetryDelayMs;
    }

    public void setMaxRetryDelayMs(Long maxRetryDelayMs) {
        this.maxRetryDelayMs = maxRetryDelayMs;
    }
}