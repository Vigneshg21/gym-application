# Gym Platform

Spring Boot application for gym operations with:

- Member onboarding and profile management
- Membership plan creation and assignment
- Invoice generation, fee collection, and payment tracking
- Membership renewal workflows
- RabbitMQ-backed notification delivery
- Scheduled fee reminders and renewal reminders
- Dashboard metrics for finance and membership health
- Separate React frontend for operations teams

## Stack

- Java 21
- Spring Boot 3.3
- Spring Web, Validation, JPA, Actuator, Mail
- RabbitMQ for async notifications
- Oracle for runtime persistence
- H2 for local tests
- React + TypeScript + Vite frontend

## Run locally

1. Start supporting services:

```bash
docker compose up -d rabbitmq
```

2. Run the backend:

```bash
mvn spring-boot:run
```

3. Run the frontend:

```bash
cd frontend
npm install
npm run dev
```

4. Useful URLs:

- Backend API base: `http://localhost:8080/api/v1`
- Frontend app: `http://localhost:5173`
- Health: `http://localhost:8080/actuator/health`
- RabbitMQ management: `http://localhost:15672` (`guest` / `guest`)

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

## Email and Telegram

- Email delivery uses Spring Mail and is configured for Gmail SMTP by default.
- Set `MAIL_USERNAME` to `gvignesh282@gmail.com` and `MAIL_PASSWORD` to a Gmail App Password, not your normal Gmail password.
- Telegram bot delivery needs `TELEGRAM_BOT_TOKEN` and `TELEGRAM_CHAT_ID`.
- A Telegram phone number alone is not enough for bot delivery. You must start a bot with BotFather and get the chat id of the target chat.
- Use `POST /api/v1/notifications/admin-test` with a JSON body like `{ "message": "Gym notification test" }` to test the configured admin email and Telegram channels.

## Notes

- WhatsApp stays in dry-run mode for now.
- The backend accepts the frontend origin through `FRONTEND_URL`, which defaults to `http://localhost:5173` for local React development.
- RabbitMQ is used as the notification backbone so reminders are decoupled from the main transaction flow.
