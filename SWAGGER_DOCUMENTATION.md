# Gym Platform API - Swagger/OpenAPI Documentation

## Overview
This document provides information about accessing the API documentation for the Gym Platform application.

## Java Version
The application has been upgraded to **Java 22**.

## Accessing the Swagger UI

Once the application is running, you can access the interactive Swagger UI documentation at:

```
http://localhost:8080/swagger-ui.html
```

## API Documentation

### Available Endpoints

The API documentation includes the following resource controllers:

1. **Members** (`/api/v1/members`)
   - Create new member
   - Update member information
   - Find member by phone number
   - List all members
   - Get member details
   - Download member card
   - Send member card notifications
   - Manage Telegram connections

2. **Memberships** (`/api/v1/memberships`)
   - Create new membership
   - List all memberships
   - Download membership card
   - Send membership notifications
   - Renew membership

3. **Membership Plans** (`/api/v1/membership-plans`)
   - Create membership plans
   - List all available plans

4. **Invoices** (`/api/v1/invoices`)
   - List invoices
   - Record payments
   - List invoice payments
   - Send receipt notifications
   - Send receipt with PDF attachment

5. **Notifications** (`/api/v1/notifications`)
   - Send announcements
   - Send admin test notifications

6. **Dashboard** (`/api/v1/dashboard`)
   - Get dashboard summary and metrics

## API Documentation URLs

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

## Features

- **Interactive API Testing**: Try out API endpoints directly from the Swagger UI
- **Request/Response Examples**: See example payloads for each endpoint
- **Parameter Documentation**: Detailed descriptions of all parameters
- **Authentication Ready**: Prepared for adding security schemes
- **Sorted Operations**: Endpoints are organized by method and sorted alphabetically

## Configuration

The Swagger/OpenAPI configuration is defined in:
- `src/main/java/com/codexgym/gym/config/OpenApiConfig.java`

Application properties in `application.yml`:
```yaml
springdoc:
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
  api-docs:
    path: /v3/api-docs
  show-actuator: true
```

## Running the Application

```bash
# Build with Maven
mvn clean install

# Run the application
mvn spring-boot:run

# Access the API at http://localhost:8080
# Access Swagger UI at http://localhost:8080/swagger-ui.html
```

## Technologies

- **Spring Boot 3.3.5**
- **Java 22**
- **SpringDoc OpenAPI 2.3.0**
- **Swagger UI** (included with SpringDoc OpenAPI)
- **PostgreSQL** (Production database)

