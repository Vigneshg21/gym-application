# Gym Platform

Spring Boot application for gym operations with:

- Member onboarding and profile management
- Membership plan creation and assignment
- Invoice generation, fee collection, and payment tracking
- Membership renewal workflows
- RabbitMQ-backed notification delivery
- Scheduled fee reminders and renewal reminders
- Dashboard metrics for finance and membership health
- Separate frontend for operations teams

## Stack

- Java 21
- Spring Boot 3.3
- Spring Web, Validation, JPA, Actuator, Mail
- PostgreSQL for runtime persistence
- RabbitMQ for async notifications
- Docker Compose for local deployment
- H2 for local tests

## Local backend run

1. Start PostgreSQL and RabbitMQ in Docker:

```bash
docker compose up -d postgres rabbitmq
```

2. Run the backend locally:

```bash
mvn spring-boot:run
```

3. Useful URLs:

- Backend API base: `http://localhost:8080/api/v1`
- Health: `http://localhost:8080/actuator/health`
- RabbitMQ management: `http://localhost:15672` (`guest` / `guest`)
- PostgreSQL: `localhost:5432` (`ajeesh` / `8072125716`, database `gym`)

## Full Docker deployment

Run the full backend stack in containers:

```bash
docker compose up --build
```

The backend will be available at `http://localhost:8080` and will connect to the Postgres container automatically.

## Environment configuration

- Runtime secrets have been moved out of `application.yml`.
- Default local database credentials are `ajeesh` / `8072125716` for the Docker Postgres container.
- Optional mail and Telegram settings can be provided through environment variables.
- `.env.example` documents the supported Docker Compose variables.

## Core backend endpoints

- `POST /api/v1/members`
- `GET /api/v1/members`
- `POST /api/v1/membership-plans`
- `GET /api/v1/membership-plans`
- `POST /api/v1/memberships`
- `GET /api/v1/memberships`
- `POST /api/v1/memberships/{membershipId}/renew`
- `GET /api/v1/invoices`
- `POST /api/v1/invoices/{invoiceId}/payments`
- `POST /api/v1/notifications/announcements`
- `POST /api/v1/notifications/admin-test`
- `GET /api/v1/dashboard`

## Notes

- WhatsApp stays in dry-run mode unless you provide a webhook and token.
- The backend accepts the frontend origin through `FRONTEND_URL`, which defaults to `http://localhost:5173`.
- The frontend codebase is not present in this workspace, so the Docker setup here covers the backend, Postgres, and RabbitMQ stack.
