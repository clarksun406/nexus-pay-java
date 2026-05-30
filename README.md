# NexusPay Java

Payment gateway orchestration system — Java 17 + Spring Boot 3 + DDD Architecture.

## Features

- **Multi-provider routing** — Stripe, Square, Braintree support with weighted-random routing rules
- **Connector management** — connect multiple accounts per provider, set weights, designate a primary
- **Complete payment lifecycle** — create → confirm → capture → refund
- **Role-based access control** — OWNER, ADMIN, DEVELOPER, FINANCE, VIEWER
- **Webhook delivery** — configurable endpoints with HMAC signing
- **API key pairs** — `pk_xxx` publishable + `sk_xxx` secret, TEST and LIVE modes
- **MFA** — TOTP-based two-factor authentication
- **3D Secure** — Support for 3DS authentication flows
- **Retry mechanism** — Automatic retry with exponential backoff

## Architecture

```
nexuspay-java/
├── nexuspay-common/           Shared utilities and configurations
├── nexuspay-domain/           Domain layer (DDD)
│   ├── aggregate/            Aggregates
│   ├── valueobject/          Value objects
│   ├── event/                Domain events
│   ├── repository/           Repository interfaces
│   └── service/              Domain services
├── nexuspay-repository/       Data access layer
├── nexuspay-service/          Application services
└── nexuspay-web/              REST API layer
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2 |
| Database | PostgreSQL 14+, Flyway migrations |
| Auth | JWT + API key authentication |
| API Doc | SpringDoc OpenAPI |
| Build | Maven |

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Database Setup

```bash
createdb nexuspay
```

### Running the Application

```bash
mvn clean install
cd nexuspay-web
mvn spring-boot:run
```

Or with Docker:

```bash
docker compose up --build
```

- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/health

## API Overview

All merchant endpoints are under `/api/v1/` and require `Authorization: Bearer sk_xxx`.

| Resource | Endpoint |
|----------|----------|
| Auth | `/api/v1/auth/` |
| Payment Intents | `/api/v1/payment-intents/` |
| Connectors | `/api/v1/connectors/` |
| Routing Rules | `/api/v1/routing-rules/` |
| API Keys | `/api/v1/api-keys/` |
| Webhooks | `/api/v1/webhooks/` |
| Refunds | `/api/v1/refunds/` |
| Payment Links | `/api/v1/payment-links/` |
| Members | `/api/v1/members/` |
| Health Monitor | `/api/v1/health/` |
| Reconciliation | `/api/v1/reconciliation/` |
| Current User | `/api/v1/me` |
| Retry Config | `/api/v1/merchants/{id}/retry-config` |
| Public Checkout | `/pub/pay/{token}` |

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing secret | (change in production) |
| `DATABASE_URL` | Database URL | `jdbc:postgresql://localhost:5432/nexuspay` |
| `DATABASE_USERNAME` | Database username | `postgres` |
| `DATABASE_PASSWORD` | Database password | `postgres` |

## DDD Architecture

This project follows Domain-Driven Design principles:

- **Aggregates** — PaymentIntentAggregate, RoutingRuleAggregate, ConnectorAggregate
- **Value Objects** — Money, PaymentStatus, ProviderType
- **Domain Events** — PaymentSucceededEvent, PaymentFailedEvent
- **Repository Pattern** — Domain repository interfaces with infrastructure implementation

## License

MIT
