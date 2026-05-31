# NexusPay Roadmap

## Version 1.0.0 (Current) - Core Platform ✅

### Backend
- [x] Multi-module Maven project structure
- [x] Domain-Driven Design architecture
- [x] PostgreSQL with Flyway migrations
- [x] JWT + API Key authentication
- [x] Multi-provider support (Stripe, Square, Braintree)
- [x] **Real provider SDK integration** (Stripe 24.0.0, Square 40.1.0, Braintree 3.31.0)
- [x] Intelligent routing with weighted selection
- [x] Payment lifecycle (create, confirm, capture, cancel)
- [x] Refund processing
- [x] 3D Secure authentication flow
- [x] Retry mechanism with exponential backoff
- [x] Health monitoring with auto-demotion
- [x] Webhook delivery with HMAC signing
- [x] **Webhook ingestion** (Stripe, Square, Braintree)
- [x] MFA (TOTP) support
- [x] Role-based access control
- [x] Payment links
- [x] **Dispute handling** with evidence submission
- [x] **Payout reconciliation** with hourly aggregation
- [x] **Embedded checkout** (/pub/tokenize)
- [x] **Rate limiting** (token bucket)
- [x] **Gateway audit logs**
- [x] **Outbox pattern** for transactional webhooks

### Frontend
- [x] Vue 3 + Vite + Tailwind CSS
- [x] Dashboard layout with navigation
- [x] Login/Register pages
- [x] Payments list and detail
- [x] Connectors management
- [x] Routing rules configuration
- [x] API keys management
- [x] Webhooks configuration
- [x] Team management
- [x] Payment link public page
- [x] **Disputes page**
- [x] **Payouts page**
- [x] **API Logs page**

### Infrastructure
- [x] Docker + docker-compose
- [x] GitHub Actions CI
- [x] Swagger/OpenAPI documentation
- [x] ~60% test coverage

### Security
- [x] Rate limiting on /auth, /pub, /api/v1/payment-intents
- [x] Gateway request logging with trace IDs
- [x] Outbox pattern for reliable webhook delivery
- [x] AES-256-GCM credential encryption
- [x] Password reset flow
- [x] MFA backup codes

---

## Version 1.1.0 - Enhanced Testing & Observability

### Testing
- [ ] Increase test coverage to 80%+
- [ ] Integration tests with Testcontainers
- [ ] E2E tests for frontend (Playwright/Cypress)
- [ ] Mutation testing (PIT)
- [ ] Load testing (JMeter/Gatling)

### Observability
- [ ] Prometheus metrics implementation
- [ ] Grafana dashboards
- [ ] Structured logging (Logback/ELK)
- [ ] Distributed tracing (OpenTelemetry)
- [ ] Health check enhancements

### Database
- [ ] Connection pooling (HikariCP tuning)
- [ ] Read replicas support
- [ ] Database migration rollback strategy

---

## Version 1.2.0 - Caching & Performance

### Caching
- [ ] Redis integration
- [ ] Cache routing rules
- [ ] Cache provider configurations
- [ ] Session storage in Redis
- [ ] Rate limiting with Redis

### Performance
- [ ] API response caching
- [ ] Database query optimization
- [ ] Async payment processing
- [ ] Webhook delivery queue

### Security
- [x] Rate limiting per IP/API key
- [ ] AES-256-GCM credential encryption
- [ ] IP allowlisting
- [ ] Encryption at rest

---

## Version 1.3.0 - Additional Providers

### New Providers
- [ ] PayPal integration
- [ ] Adyen integration
- [ ] Checkout.com integration
- [ ] Provider SDK abstraction

### Features
- [ ] Provider-specific features support
- [ ] Multi-currency handling
- [ ] Payment method tokens
- [ ] Recurring payments

---

## Version 2.0.0 - Enterprise Features

### Multi-Tenancy
- [ ] Organization-level isolation
- [ ] Custom branding per merchant
- [ ] White-label support
- [ ] Tenant-specific configurations

### Advanced Routing
- [ ] ML-based routing optimization
- [ ] Cost-based routing (fee-aware)
- [ ] Success rate prediction
- [ ] A/B testing for routing

### Analytics
- [ ] Revenue analytics dashboard
- [ ] Payment success metrics
- [ ] Provider performance comparison
- [ ] Custom reports

### Compliance
- [ ] PCI DSS compliance checklist
- [ ] GDPR data export/deletion
- [ ] SOC 2 preparation
- [ ] Data retention policies

---

## Version 3.0.0 - Cloud Native

### Kubernetes
- [ ] Helm charts
- [ ] Kubernetes operators
- [ ] Auto-scaling
- [ ] Blue-green deployments

### Cloud Provider Support
- [ ] AWS deployment (EKS, RDS, ElastiCache)
- [ ] GCP deployment (GKE, Cloud SQL, Memorystore)
- [ ] Azure deployment (AKS, Azure DB, Redis Cache)

### Disaster Recovery
- [ ] Multi-region deployment
- [ ] Database replication
- [ ] Automated backups
- [ ] Recovery procedures

---

## Feature Comparison with Original Node.js Version

| Feature | Node.js | Java | Status |
|---------|---------|------|--------|
| Payment Intent lifecycle | ✅ | ✅ | Complete |
| Multi-provider routing | ✅ | ✅ | Complete |
| Real SDK integration | ✅ | ✅ | Complete |
| 3DS authentication | ✅ | ✅ | Complete |
| Webhook ingestion | ✅ | ✅ | Complete |
| Webhook delivery | ✅ | ✅ | Complete |
| Dispute handling | ✅ | ✅ | Complete |
| Payout reconciliation | ✅ | ✅ | Complete |
| Rate limiting | ✅ | ✅ | Complete |
| Audit logging | ✅ | ✅ | Complete |
| Embedded checkout | ✅ | ✅ | Complete |
| AES-256-GCM encryption | ✅ | ✅ | Complete |
| Password reset | ✅ | ✅ | Complete |
| MFA backup codes | ✅ | ✅ | Complete |
| SMTP email | ✅ | ⚠️ | Pending |

---

## Timeline

| Version | Target Date | Status |
|---------|-------------|--------|
| 1.0.0 | Q2 2024 | ✅ Complete |
| 1.1.0 | Q3 2024 | 📋 Planned |
| 1.2.0 | Q4 2024 | 📋 Planned |
| 1.3.0 | Q1 2025 | 📋 Planned |
| 2.0.0 | Q2 2025 | 📋 Planned |
| 3.0.0 | Q4 2025 | 📋 Planned |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Feature Requests

Open an issue with the `enhancement` label or start a discussion in GitHub Discussions.
