# Distributed Job Scheduler

A distributed job scheduling and execution system built using Spring Boot, PostgreSQL, Redis, Kafka, and React.

The project is designed to explore real-world backend and distributed-systems concepts such as:

- Job scheduling
- Distributed execution
- Kafka-based asynchronous processing
- Retry mechanisms
- Exponential backoff
- Idempotency
- Concurrency control
- Failure recovery
- Distributed coordination
- Persistent execution history
- Monitoring and observability

---

## Tech Stack

### Backend

- Java 25
- Spring Boot
- Spring Data JPA
- Spring Validation
- Spring Kafka
- Spring Security
- Maven

### Databases / Infrastructure

- PostgreSQL
- Redis
- Apache Kafka

### Frontend

- React

### Observability

- Spring Boot Actuator
- Micrometer
- Prometheus
- Grafana

### Testing

- JUnit
- Mockito
- Testcontainers

### DevOps

- Docker
- Docker Compose
- GitHub Actions

---

# Architecture

```text
                         ┌───────────────┐
                         │    React UI   │
                         └───────┬───────┘
                                 │
                                 ▼
                         ┌───────────────┐
                         │  Spring Boot  │
                         │      API      │
                         └───────┬───────┘
                                 │
                                 ▼
                         ┌───────────────┐
                         │  PostgreSQL   │
                         │ Source of     │
                         │ Truth         │
                         └───────┬───────┘
                                 ▲
                                 │
                         ┌───────┴───────┐
                         │   Scheduler   │
                         └───────┬───────┘
                                 │
                                 ▼
                         ┌───────────────┐
                         │     Kafka     │
                         └───────┬───────┘
                                 │
                                 ▼
                         ┌───────────────┐
                         │    Worker     │
                         │   Executor    │
                         └───────┬───────┘
                                 │
                                 ▼
                         ┌───────────────┐
                         │ HTTP Target   │
                         │     API       │
                         └───────────────┘