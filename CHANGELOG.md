# Changelog

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
- Customers page (new)
- Subscriptions page (new)

#### Element SDK
- TypeScript SDK with Vite build
- Card Element (iframe-based)
- Payment Element (unified component)
- Setup Element (save cards)
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
- ~60% test coverage

### Documentation
- Payment Element survey (Stripe, CityPay, Adyen, Fiserv, Braintree)
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
- Basic routing
