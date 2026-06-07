# Changelog

## [Unreleased]

### Added
- Added provider refund and payment-status operations to the payment provider port.
- Added provider webhook synchronization for Stripe and Square payment events.
- Added scheduler execution for failed payment retries, subscription renewals, payout summaries, and outbox processing.
- Added repository-backed admin overview and monitoring responses.
- Added a minimal subscription renewal processing loop.

### Changed
- Moved JWT and API-key request filters from `nexuspay-common` to `nexuspay-web` so repository-backed security checks can be wired correctly.
- Tightened JWT handling so only access tokens are accepted for request authentication.
- Tightened merchant tenant checks for merchant-scoped JWT and API-key requests.
- Changed refund creation to call the selected provider instead of returning mock `re_` IDs.
- Changed reconciliation to compare local payment state with provider state.
- Changed outbox event payloads to include merchant IDs and payment status.

### Fixed
- Fixed webhook delivery retry visibility by propagating delivery failures back to outbox processing.
- Fixed subscription month/year period rollover handling.
- Fixed admin overview/monitoring endpoints that previously returned mock data.
- Fixed Java 17 compile blockers from UTF-8 BOM source files, missing module test/mail dependencies, invalid provider SDK coordinates, an invalid Flyway PostgreSQL submodule dependency, and Square SDK builder usage.
- Updated focused service tests for payment intent, refund, and retry behavior.
- Updated service and web controller tests for current domain/security dependencies, including MVC test isolation from local PostgreSQL/Flyway.

### Verification
- `mvn -DskipTests compile` passes with `JAVA_HOME=D:\Java\jdk-17`.
- `mvn -pl nexuspay-service -am -Dtest=VaultServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` passes: 4 tests, 0 failures.
- Full backend `mvn test` passes with `JAVA_HOME=D:\Java\jdk-17`: 199 tests, 0 failures, 0 errors (11 skipped: Testcontainers requires Docker).

### v1.0.2 Completed (2026-06-07)
- Regenerated Maven wrapper (`.mvn/wrapper/`, Maven 3.9.9).
- Validated Square refund/status against SDK 40.1.0.20240604.
- Validated Braintree refund/status against SDK 3.31.0.
- Added ProviderDispatcher refund/status contract tests (+7 tests).
- Added ProviderWebhookServiceTest for webhook state transitions (+13 tests).
- Added MerchantTenantSecurityTest for JWT/API-key cross-merchant enforcement (+18 tests).

### v1.3.0 Completed (2026-06-07)
- Added Testcontainers infrastructure with RepositoryIntegrationTest (11 tests, @Disabled pending Docker).
- Added RedisRateLimiter with sliding-window and counter modes, fail-open on Redis errors.
- Added RedisConfig with Jackson JSON serialization.
- Added PaymentMetrics (Micrometer/Prometheus): payment/refund/webhook counters and processing time timers.
- Added OpenTelemetry tracing bridge with 100% sampling.
- Added management endpoints for health, info, prometheus, metrics.
- Added 16 new unit tests: CustomerServiceTest (+5), InvoiceServiceTest (+5), PayoutServiceTest (+4), OutboxServiceTest (+2).
- Configured Redis as optional dependency (disabled by default).

---

## [1.0.0] - 2026-05-31

### Added

#### Core Platform
- Multi-module Maven project structure
- Domain-Driven Design architecture
- PostgreSQL with Flyway migrations
- JWT + API Key authentication
- Multi-provider support (Stripe, Square, Braintree)
- Real provider SDK integration
- Intelligent routing with weighted selection
- Payment lifecycle (create, confirm, capture, cancel)
- Refund processing
- 3D Secure authentication flow
- Retry mechanism with exponential backoff
- Health monitoring with auto-demotion
- Webhook delivery with HMAC signing
- Webhook ingestion (Stripe, Square, Braintree)
- MFA (TOTP) support
- Role-based access control
- Payment links
- Dispute handling with evidence submission
- Payout reconciliation with hourly aggregation
- Embedded checkout (/pub/tokenize)
- Rate limiting (token bucket)
- Gateway audit logs
- Outbox pattern for transactional webhooks

#### Frontend Dashboard
- Vue 3 + Vite + Tailwind CSS
- Login/Register pages
- Payments list and detail
- Connectors management
- Routing rules configuration
- API keys management
- Webhooks configuration
- Team management
- Disputes page
- Payouts page
- API Logs page
- Customers page
- Subscriptions page

#### Element SDK
- TypeScript SDK with Vite build
- Card Element (iframe-based)
- Payment Element skeleton
- Setup Element
- Apple Pay Element
- Google Pay Element
- Alipay Element
- WeChatPay Element
- Demo page

#### Subscription Module
- Customer entity and API
- PaymentMethod entity and API
- Subscription entity and API
- Customer management UI
- Subscription management UI

#### Admin Portal
- Organization management API
- Admin dashboard API
- Admin overview page
- Organizations page
- Admin layout with navigation

### Security
- Rate limiting on /auth, /pub, /api/v1/payment-intents
- Gateway request logging with trace IDs
- Outbox pattern for reliable webhook delivery

### Infrastructure
- Docker + docker-compose
- GitHub Actions CI
- Swagger/OpenAPI documentation
- Initial service and controller test coverage

### Documentation
- Payment Element survey
- Elements implementation guide
- Elements roadmap
- RBAC design document
- API reference
- SDK integration guide
- Deployment guide
- Database design
- Security design

---

## [0.9.0] - 2026-05-15

### Added
- Initial project structure
- Basic payment intent flow
- Stripe integration
