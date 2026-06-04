# NexusPay Java

Payment gateway orchestration system built with Java 17, Spring Boot 3, PostgreSQL, and Vue.

NexusPay provides a single API in front of Stripe, Square, and Braintree. It includes payment intent lifecycle management, provider routing, merchant dashboard foundations, admin foundations, webhook processing, refunds, subscriptions, and an embeddable Elements SDK skeleton.

## Current Status

The project is in v1 stabilization. Core modules and many workflows are implemented, but the system still needs a Java 17 compile/test pass, provider SDK signature validation, admin auth, permission-backed RBAC, and broader integration coverage before production use.

Recent high-priority backend stabilization added:
- Web-layer JWT and API-key filters with merchant tenant checks.
- Access-token-only request authentication.
- Provider-dispatched refunds instead of mock refund IDs.
- Provider status lookup and provider-status based reconciliation.
- Stripe and Square payment webhook state synchronization.
- Outbox payloads with merchant context and retry-visible delivery failures.
- Scheduler execution for health checks, retries, renewals, payouts, and outbox processing.
- Repository-backed admin overview and monitoring data.
- Minimal subscription renewal processing.

See [ROADMAP.md](ROADMAP.md) and [docs/nexuspay-todo-list.md](docs/nexuspay-todo-list.md) for the current remaining work.

## Requirements

| Tool | Version |
|------|---------|
| Java | 17 |
| Maven | 3.9+ recommended |
| PostgreSQL | 16 recommended |
| Node.js | 18+ recommended |

Note: Java 8 is not sufficient. A local JDK such as `D:\Java\jdk1.8.0_202` will fail to compile this project.

## Modules

| Module | Purpose |
|--------|---------|
| `nexuspay-domain` | Entities and domain model |
| `nexuspay-repository` | Spring Data repositories |
| `nexuspay-service` | Business services and provider port |
| `nexuspay-infra` | Provider SDK adapters |
| `nexuspay-common` | Shared utilities |
| `nexuspay-web` | Spring Boot API, controllers, security filters |
| `frontend-dashboard` | Merchant dashboard |
| `frontend-admin` | Platform admin console |
| `frontend-nexuspay-js` | Embeddable Elements SDK |

## Key Features

- Payment intents: create, confirm, capture, cancel, and status tracking.
- Provider routing across Stripe, Square, and Braintree connectors.
- Full and partial refunds through provider adapters.
- API-key and JWT authentication for merchant workflows.
- Merchant tenant checks on scoped API routes.
- Inbound provider webhook verification and local state updates.
- Outbound webhook delivery with HMAC signing, outbox persistence, and retries.
- Disputes, payouts, subscriptions, customers, payment methods, and payment links.
- Admin overview and provider monitoring foundations.
- Vue merchant dashboard and separate Vue admin console.
- TypeScript Elements SDK foundations.

## Quick Start

### Docker

```bash
docker compose up --build
```

Default local URLs:

| Service | URL |
|---------|-----|
| Frontend dashboard | `http://localhost:5173` |
| Backend API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |

### Local Backend

```bash
java -version
mvn -DskipTests compile
cd nexuspay-web
mvn spring-boot:run
```

The Maven wrapper file exists, but the wrapper support directory is currently missing. Use system Maven until the wrapper is restored.

### Local Frontend

```bash
cd frontend-dashboard
npm install
npm run dev
```

For the admin console:

```bash
cd frontend-admin
npm install
npm run dev
```

## API Overview

### Authentication

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/auth/register` | POST | Register a merchant user |
| `/api/v1/auth/login` | POST | Login |
| `/api/v1/auth/refresh` | POST | Refresh tokens |
| `/api/v1/auth/mfa/*` | POST | MFA operations |

### Payments

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/payment-intents` | POST | Create a payment intent |
| `/api/v1/payment-intents/{id}/confirm` | POST | Confirm and charge |
| `/api/v1/payment-intents/{id}/capture` | POST | Capture a manual payment |
| `/api/v1/payment-intents/{id}/cancel` | POST | Cancel a payment |
| `/api/v1/refunds` | POST | Create a refund |

### Merchant Dashboard

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/merchants/{id}/payment-intents` | GET | List payments |
| `/api/v1/merchants/{id}/disputes` | GET | List disputes |
| `/api/v1/merchants/{id}/payouts` | GET | List payouts |
| `/api/v1/merchants/{id}/connectors` | CRUD | Manage connectors |
| `/api/v1/merchants/{id}/routing-rules` | CRUD | Manage routing rules |
| `/api/v1/merchants/{id}/api-keys` | CRUD | Manage API keys |
| `/api/v1/merchants/{id}/webhook-endpoints` | CRUD | Manage webhooks |
| `/api/v1/merchants/{id}/customers` | CRUD | Manage customers |
| `/api/v1/merchants/{id}/subscriptions` | CRUD | Manage subscriptions |

### Public and Provider Webhooks

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/pub/pay/{token}` | GET/POST | Hosted payment link |
| `/pub/tokenize` | POST | Embedded checkout tokenization |
| `/webhooks/stripe` | POST | Stripe webhook |
| `/webhooks/square` | POST | Square webhook |
| `/webhooks/braintree` | POST/GET | Braintree webhook |

## Environment Variables

| Variable | Notes |
|----------|-------|
| `JWT_SECRET` | Required; use at least 256 bits |
| `DATABASE_URL` | PostgreSQL connection URL |
| `DATABASE_USERNAME` | PostgreSQL username |
| `DATABASE_PASSWORD` | PostgreSQL password |
| `PAY_BASE_URL` | Public backend URL for provider webhooks |

## Verification

After configuring JDK 17:

```bash
mvn -DskipTests compile
mvn test
```

Known current verification gap: the local Java 8 environment cannot compile the project, and Square/Braintree adapter signatures still need a Java 17 compile pass with SDK dependencies available.

## Documentation

- [Roadmap](ROADMAP.md)
- [Current TODO list](docs/nexuspay-todo-list.md)
- [API reference](docs/api-reference.md)
- [Database design](docs/database-design.md)
- [Security design](docs/security-design.md)
- [Frontend structure](docs/frontend-structure.md)
- [Elements SDK guide](docs/element-sdk-guide.md)

## License

MIT
