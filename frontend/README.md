# Gym Platform Frontend

Responsive React + Vite dashboard for the Codex Gym backend.

## Features

- Overview metrics for members, collections, receivables, and renewals
- Member onboarding form and searchable roster
- Membership plan creation and plan catalogue
- Membership enrollment and renewal flows
- Invoice search and payment collection form
- WhatsApp announcement queue trigger for backend messaging
- Mobile-friendly responsive layout for tablet and phone use

## Environment

Create a `.env` file from `.env.example` if you need to point at another backend.

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Local development

```bash
npm install
npm run dev
```

## Production build

```bash
npm run build
```

The frontend expects the backend to allow the frontend origin through CORS. The Spring Boot app already defaults `FRONTEND_URL` to `http://localhost:5173`.
