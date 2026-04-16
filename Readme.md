# Spring Boot Kafka Email Sender

Asynchronous email automation using Spring Boot, Kafka, MySQL, and SMTP (Mailhog). The system accepts REST requests, queues them in Kafka, consumes messages to send templated emails, retries on failure with exponential backoff, and persists status to MySQL.

## Modules
- `common`: Shared entities, DTOs, repository.
- `email-producer`: REST API, scheduling, Kafka publishing.
- `email-consumer`: Kafka listener, email delivery, retries/DLQ.

## Prerequisites
- Java 17+
- Docker & Docker Compose

## Run with Docker Compose
```bash
docker-compose up --build
```
Services:
- Producer API: http://localhost:8080
- Consumer service: http://localhost:8081
- Mailhog UI: http://localhost:8025 (SMTP on 1025)
- MySQL: localhost:3306 (`emaildb` / `email` / `email123`)
- Kafka: localhost:9092

## Local development
```bash
./gradlew clean build
./gradlew :email-producer:bootRun   # in one terminal
./gradlew :email-consumer:bootRun   # in another terminal
```

Set env vars as needed (e.g., `DB_HOST`, `KAFKA_BOOTSTRAP`, `SMTP_HOST`).

## API
- **POST** `/api/emails/send`
```json
{
  "to": "user@example.com",
  "subject": "Welcome!",
  "body": "Hello {{name}}",
  "template": "welcome",
  "variables": { "name": "John" },
  "scheduledAt": "2026-04-20T10:00:00"
}
```
- **GET** `/api/emails/{id}` — returns status and retry count.

Swagger UI is available at `/swagger-ui.html` on the producer service.

## Kafka Topics
- `email-requests` (primary)
- `email-retry` (retry)
- `email-dlq` (dead-letter)

## Retry & Scheduling
- Up to 3 attempts with backoff (5s → 15s → 30s).
- Scheduled emails (`scheduledAt` in the future) are dispatched by a scheduler.
- Failed final attempts are pushed to `email-dlq`.

## Templates
- Thymeleaf templates live in `email-consumer/src/main/resources/templates/` (sample `welcome.html` provided).

## Database
`email_requests` table tracks status (`PENDING`, `SCHEDULED`, `QUEUED`, `SENT`, `FAILED`, `DLQ`), retry count, timestamps, variables JSON, and correlation ID.

## Testing
Run the full test suite:
```bash
./gradlew test
```
