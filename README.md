# NexusPay Java

Payment gateway orchestration system built with Java 17, Spring Boot 3, and Domain-Driven Design architecture.

## Overview

NexusPay is a production-ready payment gateway that supports multiple payment providers (Stripe, Square, Braintree) with intelligent routing, comprehensive payment lifecycle management, and a complete merchant dashboard.

## Features

### Core Payment Features
- **Multi-Provider Routing** — Intelligent routing with weighted-random selection across Stripe, Square, and Braintree
- **Payment Lifecycle** — Complete flow: create → confirm → capture → refund
- **Connector Management** — Multiple accounts per provider with primary/weight configuration
- **3D Secure** — Full 3DS authentication flow support
- **Retry Mechanism** — Automatic retry with exponential backoff and fallback providers

### Security & Authentication
- **JWT + API Key** — Dual authentication system with access/refresh tokens
- **MFA** — TOTP-based two-factor authentication
- **Role-Based Access** — OWNER, ADMIN, DEVELOPER, FINANCE, VIEWER

### Operations
- **Webhook Delivery** — Configurable endpoints with HMAC signing
- **Health Monitoring** — Real-time connector health tracking with auto-demotion
- **Reconciliation** — Transaction import and discrepancy detection

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security |
| Architecture | Domain-Driven Design (DDD) |
| Database | PostgreSQL 16, Flyway migrations |
| Frontend | Vue 3, Vite, Tailwind CSS, Pinia |
| API Doc | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven multi-module |
| CI/CD | GitHub Actions |

## Architecture

```
nexuspay-java/
├── nexuspay-common/           # Shared utilities
│   ├── config/                # Security configuration
│   ├── exception/             # Exception handling
│   ├── security/              # Auth filters
│   └── util/                  # Crypto, JWT utilities
│
├── nexuspay-domain/           # Domain layer (DDD)
│   ├── aggregate/             # Aggregates (PaymentIntent, Connector, RoutingRule)
│   ├── valueobject/           # Value objects (Money, PaymentStatus, ProviderType)
│   ├── event/                 # Domain events
│   ├── repository/            # Repository interfaces
│   └── service/               # Domain services
│
├── nexuspay-repository/       # Data access layer
│   └── impl/                  # Repository implementations
│
├── nexuspay-service/          # Application services
│   ├── AuthService
│   ├── PaymentIntentService
│   ├── RoutingEngine
│   ├── HealthMonitorService
│   └── ...
│
├── nexuspay-web/              # REST API layer
│   ├── controller/            # 14 REST controllers
│   ├── config/                # Web & OpenAPI config
│   └── resources/
│       └── db/migration/      # Flyway migrations
│
└── frontend/                  # Vue 3 Dashboard
    └── src/
        ├── pages/             # 14 page components
        ├── stores/            # Pinia stores
        └── router/            # Vue Router config
```

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Node.js 18+ (for frontend development)

### Using Docker (Recommended)

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Manual Setup

**1. Database**
```bash
createdb nexuspay
```

**2. Backend**
```bash
mvn clean install
cd nexuspay-web
mvn spring-boot:run
```

**3. Frontend**
```bash
cd frontend
npm install
npm run dev
```

**4. Create Account**

Open http://localhost:5173 and register. The first registration creates a merchant account with OWNER role.

## API Reference

### Authentication
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/auth/register` | POST | Register new user |
| `/api/v1/auth/login` | POST | Login |
| `/api/v1/auth/refresh` | POST | Refresh tokens |

### Payments
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/payment-intents` | POST | Create payment intent |
| `/api/v1/payment-intents/{id}/confirm` | POST | Confirm payment |
| `/api/v1/payment-intents/{id}/capture` | POST | Capture payment |
| `/api/v1/payment-intents/{id}/cancel` | POST | Cancel payment |
| `/api/v1/refunds` | POST | Create refund |

### Configuration
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/connectors` | CRUD | Manage payment connectors |
| `/api/v1/routing-rules` | CRUD | Manage routing rules |
| `/api/v1/api-keys` | CRUD | Manage API keys |
| `/api/v1/webhooks` | CRUD | Manage webhook endpoints |

### Public
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/pub/pay/{token}` | GET/POST | Public payment link |

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing secret | (required in production) |
| `DATABASE_URL` | PostgreSQL URL | `jdbc:postgresql://localhost:5432/nexuspay` |
| `DATABASE_USERNAME` | Database user | `postgres` |
| `DATABASE_PASSWORD` | Database password | `postgres` |

## Testing

```bash
mvn test                          # Run all tests
mvn verify                        # Run with integration tests
```

### Test Coverage: ~60%
- Domain layer: Aggregates, Value Objects
- Service layer: Business logic
- Web layer: Controller authentication
- Common: Utilities, Exception handling

## Project Stats

| Metric | Value |
|--------|-------|
| Java Files | 94 |
| Vue Components | 16 |
| Lines of Code | 7,200+ |
| Test Classes | 18 |
| Maven Modules | 5 |
| REST Endpoints | 40+ |

## License

MIT

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
