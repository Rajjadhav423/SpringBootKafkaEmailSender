# Spring Boot Kafka Email Sender

A production-ready multi-module Spring Boot application that uses Apache Kafka for asynchronous email sending.

## Architecture

```
┌─────────────────┐    Kafka Topic     ┌──────────────────┐
│  email-producer │ ──email-requests──▶│  email-consumer  │
│  (REST API)     │                    │  (Mail Sender)   │
└────────┬────────┘                    └────────┬─────────┘
         │                                      │
         ▼                                      ▼
    MySQL DB                              MySQL DB + SMTP
```

### Modules
- **common** – Shared DTOs, events, and enums
- **email-producer** – REST API that accepts email requests and publishes to Kafka
- **email-consumer** – Kafka listener that sends emails via JavaMailSender

## Features
- Asynchronous email sending via Kafka
- Scheduled email support
- Automatic retry with exponential backoff (`@RetryableTopic`)
- Dead Letter Queue (DLQ) for failed messages
- OpenAPI / Swagger UI documentation
- Template variable substitution (`{{name}}`)
- Idempotency via correlation IDs
- MySQL persistence in both producer and consumer

## Tech Stack
| Technology | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.4 |
| Apache Kafka | (via Spring Kafka) |
| MySQL | 8.0 |
| Gradle | 8.7 |
| Lombok | (via Spring Boot BOM) |
| springdoc-openapi | 2.3.0 |

## Prerequisites
- Java 17+
- Docker & Docker Compose

## Quick Start

### 1. Clone and build
```bash
git clone <repo-url>
cd SpringBootKafkaEmailSender
./gradlew build
```

### 2. Start all services with Docker Compose
```bash
docker-compose up --build
```

Services started:
| Service | URL |
|---|---|
| email-producer | http://localhost:8080 |
| email-consumer | http://localhost:8081 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| MailHog UI | http://localhost:8025 |
| Kafka | localhost:9092 |
| MySQL | localhost:3306 |

## API Reference

### Send Email
```http
POST /api/emails/send
Content-Type: application/json

{
  "to": "user@example.com",
  "subject": "Welcome!",
  "body": "Hello {{name}}, welcome to {{company}}!",
  "variables": {
    "name": "John",
    "company": "Acme Corp"
  }
}
```

**Response** `202 Accepted`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "to": "user@example.com",
  "subject": "Welcome!",
  "status": "PENDING",
  "retryCount": 0,
  "createdAt": "2024-01-15T10:30:00"
}
```

### Send Scheduled Email
```http
POST /api/emails/send
Content-Type: application/json

{
  "to": "user@example.com",
  "subject": "Reminder",
  "body": "Your appointment is tomorrow.",
  "scheduledAt": "2024-12-25T09:00:00"
}
```

### Get Email Status
```http
GET /api/emails/{id}
```

**Response** `200 OK`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "to": "user@example.com",
  "subject": "Welcome!",
  "status": "SENT",
  "retryCount": 0,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:05"
}
```

## Email Status Values
| Status | Description |
|---|---|
| `PENDING` | Email queued for sending |
| `SCHEDULED` | Email scheduled for future delivery |
| `SENT` | Email sent successfully |
| `FAILED` | Email failed after all retries |
| `RETRY` | Email is being retried |

## Kafka Topics
| Topic | Purpose |
|---|---|
| `email-requests` | Main email processing topic (3 partitions) |
| `email-retry` | Retry topic |
| `email-dlq` | Dead Letter Queue for failed messages |

## Running Tests
```bash
./gradlew test
```

## Building Individual Modules
```bash
./gradlew :email-producer:build
./gradlew :email-consumer:build
```

## Configuration

### email-producer environment variables
| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `emaildb` | Database name |
| `DB_USER` | `root` | Database user |
| `DB_PASSWORD` | `root` | Database password |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |

### email-consumer additional variables
| Variable | Default | Description |
|---|---|---|
| `MAIL_HOST` | `localhost` | SMTP host |
| `MAIL_PORT` | `1025` | SMTP port |
| `MAIL_USERNAME` | _(empty)_ | SMTP username |
| `MAIL_PASSWORD` | _(empty)_ | SMTP password |
