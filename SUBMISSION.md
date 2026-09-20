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

Report the observed result, including relevant counts, terminal states, or mismatches. Do not report an expected result as though it was observed.

Describe the failure or recovery scenario demonstrated in your video and how a reviewer can reproduce it.

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

## Technology choices

Why did you choose this stack? What alternatives did you consider? What trade-offs did you accept?

## Important decisions

Describe two or three decisions that materially shaped the solution.

## Assumptions and limitations

List relevant assumptions, known limitations, and deliberately unfinished work.

## Production and scale

If this prototype needed to operate in production or at significantly greater scale, what would you change first and why? Clearly distinguish what the submitted implementation does now from improvements you are proposing.

## AI usage

List any AI tools used, explain how they contributed, and describe how you reviewed or tested their output. If you did not use AI tools, say so.

## Credibility note

Describe one product or system you previously helped ship:

- The problem it solved
- Your personal contribution
- The scale or operational complexity involved
- One difficult engineering or product decision
- A public link or other evidence, when available

Confidential details may be anonymized and figures may be approximate.
