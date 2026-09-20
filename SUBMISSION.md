# Product Engineering Challenge Submission

## Candidate

- **Name:** Dhiraj Kumar
- **Email:** dhirajkr0901@gmail.com
- **GitHub:** https://github.com/dhirajkumar91
- **Selected problem:** Problem 3 — Durable Reminders and Follow-Ups

## Run the project

### Prerequisites

- Java 21
- PostgreSQL
- Maven Wrapper

Create a PostgreSQL database named:

```sql
CREATE DATABASE durable_reminders;

```
Configure the PostgreSQL connection in:

durable-reminders/backend/src/main/resources/application.properties

Start the application

From the backend directory:

cd durable-reminders/backend
./mvnw spring-boot:run

On Windows PowerShell:

cd durable-reminders/backend
.\mvnw.cmd spring-boot:run

The application starts on:

http://localhost:8080

Successful scenario

Create a reminder using:

POST /api/reminders
Content-Type: application/json

Example:

{
  "content": "Prepare Caygnus interview",
  "scheduledAt": "2026-09-21T00:30",
  "timezone": "Asia/Kolkata"
}

## Run the tests

```text
./mvnw clean test
./mvnw -Dtest=ReminderBenchmarkTest test

Observed Result
========== REMINDER BENCHMARK ==========
Reminders          : 20
Timezones          : Asia/Kolkata, America/New_York
Delivered          : 20
Attempt records    : 20
Logical deliveries : 20
Execution time     : 743 ms
==========================================
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS```

## Acceptance scenarios and verification

List the acceptance scenarios you completed. For anything incomplete or intentionally interpreted differently, explain the current behaviour and your reasoning.

Provide the exact command or steps used to run the problem-specific verification benchmark:

```text

```

## Architecture and data flow

## Architecture and data flow

The system uses a simple layered architecture:

```text
                    ┌─────────────────────┐
                    │   REST Controller   │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   Reminder Service  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   JPA Repositories  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     PostgreSQL      │
                    └─────────────────────┘


              Scheduled polling
                     │
                     ▼
            ┌───────────────────┐
            │ Reminder Scheduler│
            └─────────┬─────────┘
                      │
                      ▼
        ┌────────────────────────────┐
        │ Reminder Execution Service │
        └──────────────┬─────────────┘
                       │
                ┌──────┴──────┐
                │             │
                ▼             ▼
        ┌──────────────┐  ┌────────────────────┐
        │   Attempts   │  │ Notification       │
        │              │  │ Destination        │
        └──────┬───────┘  └─────────┬──────────┘
               │                    │
               └──────────┬─────────┘
                          ▼
                     PostgreSQL

For reminder creation:
HTTP Request
     ↓
ReminderController
     ↓
ReminderService
     ↓
TimeService
     ↓
ReminderRepository
     ↓
PostgreSQL

Scheduled execution flow:
Application Scheduler
        ↓
Find due SCHEDULED reminders
        ↓
Database-backed claim
        ↓
SCHEDULED → RUNNING
        ↓
Create execution attempt
        ↓
Notification Destination
        ↓
Success / Temporary Failure / Permanent Failure
        ↓
Persist result

Recovery flow
Application restart
        ↓
Find RUNNING reminders
        ↓
Recover interrupted work
        ↓
RUNNING → SCHEDULED
        ↓
Normal scheduler processing
```
## Technology choices

### Java 21

Java 21 was selected because it provides a strongly typed, mature runtime and works well with the selected Spring Boot stack.

### Spring Boot

Spring Boot was selected for:

- REST API development
- dependency injection
- transaction management
- scheduled execution
- Spring Data JPA
- automated testing

It also keeps the implementation relatively small and easy to reason about.

### PostgreSQL

PostgreSQL is used as the durable source of truth for:

- reminder state
- scheduled execution time
- execution attempts
- notification deliveries

PostgreSQL also provides transactions and database-level unique constraints, which are useful for implementing durable idempotency.

### Maven

Maven Wrapper is included so that the project can be built and tested without requiring a separately installed Maven version.

### Alternatives considered

I considered using technologies such as:

- Redis
- Kafka
- Quartz
- a distributed workflow engine

These were intentionally not introduced.

The assessment focuses on durable scheduling, recovery, retries, timezone correctness, and idempotency. Introducing additional infrastructure would increase setup and operational complexity without being necessary to demonstrate these requirements.

The PostgreSQL-backed design keeps the important correctness properties visible and testable.

## Important decisions

### 1. PostgreSQL is the source of truth

Reminder state is persisted in PostgreSQL rather than being maintained only in memory.

This means reminder state survives application restarts.

The scheduler discovers due work from the database instead of relying on an in-memory timer as the authoritative schedule.

### 2. Explicit reminder state machine

The implementation uses explicit states:

```text
SCHEDULED
RUNNING
DELIVERED
CANCELLED
FAILED
```

## Assumptions and limitations

### Assumptions

- PostgreSQL is available to the application.
- Reminder creation and editing require a future scheduled time.
- Timezones supplied by the API are valid IANA timezone identifiers.
- PostgreSQL is the authoritative persistent store.
- The fake notification destination represents the external notification provider.
- Temporary delivery failures are retryable.
- Permanent delivery failures are terminal.
- Interrupted `RUNNING` reminders can be recovered after application restart.

### Limitations

The following features are intentionally outside the scope of this implementation:

- Recurring reminders
- Natural-language reminder parsing
- Real email/SMS/push notification providers
- Authentication
- Authorization
- Multi-tenancy
- Distributed workflow orchestration
- Polished frontend dashboard
- Advanced production observability
- High-availability deployment
- Dedicated JMH performance benchmarking

The scheduler is intentionally simple and runs inside the Spring Boot application.

The current implementation is primarily designed around a single application process. The database-backed claim protects against concurrent execution claiming the same scheduled reminder, but a large multi-instance production deployment would require additional worker coordination and operational considerations.

## Production and scale

### Current implementation

The current implementation consists of:

```text
Spring Boot
    +
PostgreSQL
    +
Database-backed scheduler
    +
Fake notification destination
```

## AI usage

AI tools were used during development.

ChatGPT was used for:

- discussing the system architecture
- designing the reminder state machine
- designing the persistence model
- scaffolding parts of the Spring Boot implementation
- debugging compilation and runtime issues
- designing test scenarios
- reasoning about timezone handling
- reasoning about retry and idempotency behavior
- reasoning about restart recovery
- reviewing implementation decisions
- preparing the submission documentation

The implementation was reviewed, modified, executed, and tested locally.

Verification was performed using:

- automated tests
- REST API testing
- PostgreSQL inspection
- application restart/recovery testing
- scheduler execution
- failure/retry testing
- duplicate-delivery testing
- benchmark execution

AI assistance was used as a development aid, while the final implementation and its observed behavior were locally verified.

## Credibility note

### DSTTE Bihar Internship Portal

Public system:

https://internship.bihar.gov.in/

I contributed to the DSTTE Bihar Internship Portal by developing a certificate generation system for students who successfully completed their internships.

The certificate-generation functionality allows certificates to be generated for students after completion of their internship and forms part of the portal's internship workflow.

The portal is used by students and recruiters as part of the Bihar internship program.

My contribution focused on the certificate-generation system and its supporting frontend and backend implementation.

Public evidence:

https://internship.bihar.gov.in/
