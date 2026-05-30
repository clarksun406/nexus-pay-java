# NexusPay Java

Payment gateway orchestration system built with Java 17, Spring Boot 3, and Domain-Driven Design architecture.

## Overview

NexusPay is a production-ready payment gateway that provides a single, consistent API in front of Stripe, Square, and Braintree with intelligent routing, comprehensive payment lifecycle management, and a complete merchant dashboard.

**NexusPay does not process card transactions directly. It sits between your code and the card networks' acquirers**, providing:

- A single, consistent API for payments, refunds, captures and cancellations
- Intelligent routing across multiple PSP accounts (failover, weighted random, or cheapest-by-fees)
- A merchant dashboard for ops users (payments, refunds, disputes, payouts, connectors, members)
- Hosted payment links and an embedded checkout flow with token vaulting
- Outbound + inbound webhook processing, with HMAC signing, idempotency and retries
- Multi-tenant RBAC down to the merchant level

## Features

### Payments
- **PaymentIntent state machine** — REQUIRES_PAYMENT_METHOD → REQUIRES_CONFIRMATION → PROCESSING → REQUIRES_ACTION → REQUIRES_CAPTURE → SUCCEEDED / FAILED / CANCELED
- **Idempotency keys** — Safe replay returns existing intent
- **Manual / automatic capture** — Stripe capture_method, Square autocomplete, Braintree authorizePaymentMethod
- **3DS / SCA** — REQUIRES_ACTION with next_action URL for hosted checkout
- **Refunds** — Full or partial, with webhook events

### Connectors & Routing
- **Multi-connector support** — Multiple accounts per provider (e.g., two Stripe accounts)
- **Per-connector fee config** — { fixed, percentage } for routing and payouts
- **Routing rules** — Match by currency, amount range, country, payment method type
- **Selection strategies** — Weighted-random (default) or cheapest-by-fees
- **Fallback connector** — Per-rule fallback support

### Checkout
- **Payment links** — `/pub/pay/:token` for hosted checkout
- **Embedded flow** — `/pub/tokenize` returns gw_tok_… for secret-key flow

### Dispute Handling
- **Inbound webhooks** — Ingest disputes from all providers
- **Evidence submission** — Draft + submit to Stripe
- **Status tracking** — OPEN, UNDER_REVIEW, WON, LOST, etc.

### Reconciliation
- **Payout summaries** — Hourly aggregation per (merchant, connector, currency, mode)
- **Itemized breakdown** — Per-payment details

### Webhooks
- **Outbound** — HMAC-SHA256 signing with X-NexusPay-Signature
- **Inbound** — Stripe, Square, Braintree signature verification
- **Transactional outbox** — Reliable delivery with retries

### Security
- **Rate limiting** — Token bucket on /auth, /pub, /api/v1/payment-intents
- **Audit logging** — gateway_logs with trace IDs
- **RBAC** — OWNER, ADMIN, DEVELOPER, FINANCE, VIEWER
- **MFA** — TOTP-based two-factor authentication

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security |
| Architecture | Domain-Driven Design (DDD) |
| Database | PostgreSQL 16, Flyway migrations |
| Providers | Stripe SDK 24.0.0, Square SDK 40.1.0, Braintree SDK 3.31.0 |
| Frontend | Vue 3, Vite, Tailwind CSS, Pinia |
| API Doc | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven multi-module |
| CI/CD | GitHub Actions |

## Architecture

```
                        ┌────────────────────────────────────────────────┐
                        │                  Frontend (Vue 3)              │
                        │   Dashboard · Hosted Pay · Embedded Checkout   │
                        └──────────────┬─────────────────────────────────┘
                                       │ HTTPS
                                       ▼
   ┌───────────────────────────────────────────────────────────────────────┐
   │                          Backend API (Spring Boot)                   │
   │                                                                      │
   │   /api/v1/auth      /api/v1/me        /api/v1/payment-intents        │
   │   /api/v1/merchants /pub/...          /webhooks/{stripe,square,bt}   │
   │                                                                      │
   │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  │
   │  │ Routing      │→ │ Provider     │  │ Webhook      │  │ Payout   │  │
   │  │ Engine       │  │ Dispatcher   │  │ Worker       │  │ Worker   │  │
   │  └──────────────┘  └──────┬───────┘  └──────┬───────┘  └────┬─────┘  │
   └─────────────────────────────┼─────────────────┼───────────────┼──────┘
                                 │                 │               │
                                 ▼                 ▼               ▼
                       ┌─────────────────┐ ┌──────────────────┐ ┌──────────┐
                       │ Stripe / Square │ │  Postgres        │ │ Postgres │
                       │ Braintree APIs  │ │  (intents,       │ │ (payouts)│
                       └─────────────────┘ │  outbox, logs…)  │ └──────────┘
                                           └──────────────────┘
```

## Quick Start

### Docker (Recommended)

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Local Development

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

Open http://localhost:5173 and register. The first registration creates a merchant account with OWNER role.

## API Reference

### Authentication
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/auth/register` | POST | Register new user |
| `/api/v1/auth/login` | POST | Login |
| `/api/v1/auth/refresh` | POST | Refresh tokens |
| `/api/v1/auth/mfa/*` | POST | MFA setup/confirm/disable |

### Payments (sk_* required)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/payment-intents` | POST | Create payment intent (idempotent) |
| `/api/v1/payment-intents/:id/confirm` | POST | Confirm and charge |
| `/api/v1/payment-intents/:id/capture` | POST | Manual capture |
| `/api/v1/payment-intents/:id/cancel` | POST | Cancel payment |
| `/api/v1/refunds` | POST | Create refund |

### Dashboard (JWT required)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/merchants/:id/payment-intents` | GET | List payments |
| `/api/v1/merchants/:id/disputes` | GET | List disputes |
| `/api/v1/merchants/:id/payouts` | GET | List payouts |
| `/api/v1/merchants/:id/connectors` | CRUD | Manage connectors |
| `/api/v1/merchants/:id/routing-rules` | CRUD | Manage routing rules |
| `/api/v1/merchants/:id/api-keys` | CRUD | Manage API keys |
| `/api/v1/merchants/:id/webhook-endpoints` | CRUD | Manage webhooks |

### Public (no auth)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/pub/pay/:token` | GET/POST | Hosted payment link |
| `/pub/tokenize` | POST | Embedded checkout tokenization |
| `/webhooks/stripe` | POST | Stripe webhook |
| `/webhooks/square` | POST | Square webhook |
| `/webhooks/braintree` | POST/GET | Braintree webhook |

## Provider Setup

### Stripe
1. Get secret key (sk_test_… or sk_live_…)
2. Connectors → New connector → Stripe
3. Configure webhook at `${PAY_BASE_URL}/webhooks/stripe`

### Square
1. Get access token and location ID
2. Connectors → New connector → Square
3. Configure webhook signature key

### Braintree
1. Get public/private key pair
2. Connectors → New connector → Braintree
3. Webhook verification handled automatically

## Environment Variables

| Variable | Default | Notes |
|----------|---------|-------|
| `JWT_SECRET` | required | At least 256 bits |
| `DATABASE_URL` | localhost | PostgreSQL connection |
| `DATABASE_USERNAME` | postgres | |
| `DATABASE_PASSWORD` | postgres | |

## Project Stats

| Metric | Value |
|--------|-------|
| Java Files | 115+ |
| Vue Components | 19 |
| Lines of Code | 8,500+ |
| Test Classes | 19 |
| Maven Modules | 5 |
| REST Endpoints | 50+ |

## License

MIT

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
