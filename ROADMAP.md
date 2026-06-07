# NexusPay Roadmap

Last updated: 2026-06-07

## Current Status

NexusPay Java is in a v1 stabilization phase. The core backend modules, provider adapter surface, dashboard APIs, Elements SDK skeleton, admin foundations, and v1.5.0 Card Vault exist, but the project should not be treated as fully production-ready until the remaining security, integration, and broad regression test gaps are closed.

The current high-priority stabilization work has passed Java 17 backend compile and full backend `mvn test` regression passes under JDK 17.

## v1.0.1 - High-Priority Stabilization

Status: implemented; Java 17 compile verified on 2026-06-07.

Completed in this cycle:
- Moved request authentication filters into the web module and restored request context population for JWT and API-key flows.
- Enforced merchant path/header ownership checks for authenticated dashboard and secret-key requests.
- Added access-token validation so refresh tokens are not accepted as request authentication.
- Replaced mock refund IDs with provider-dispatched refunds through the provider port.
- Added provider refund and payment-status abstractions for Stripe, Square, and Braintree adapters.
- Added provider webhook state synchronization for Stripe and Square payment events.
- Updated outbound outbox payloads to include merchant IDs and terminal payment status.
- Made webhook delivery failures visible to the outbox retry loop.
- Connected the scheduler to health checks, payout summaries, failed-payment retries, subscription renewals, and outbox processing.
- Added provider-status based reconciliation instead of returning an always-empty discrepancy list.
- Replaced admin overview and monitoring mock data with repository/provider aggregates.
- Added a minimal subscription renewal loop with period rollover handling.
- Updated focused service tests for the changed refund, payment intent, and retry behavior.

Verification notes:
- `mvn -DskipTests compile` passes with `JAVA_HOME=D:\Java\jdk-17`.
- Square and Braintree adapter dependencies now resolve with their corrected Maven coordinates.
- Square create-payment construction has been aligned with the SDK 40.1.0.20240604 builder signature.
- `mvnw.cmd` exists, but the Maven wrapper support directory is still missing, so use system Maven after configuring JDK 17.

## v1.0.2 - Build and Provider Verification

Status: partially completed.

Priority: high.

Completed on 2026-06-07:
- Configured local Maven runs to use `D:\Java\jdk-17`.
- Ran `mvn -DskipTests compile` successfully across all backend modules.
- Ran the full backend `mvn test` suite successfully across all backend modules.
- Fixed Java source BOM issues that blocked `javac`.
- Validated Square and Braintree SDK dependency resolution during the Java 17 compile pass.
- Removed an invalid Flyway PostgreSQL submodule dependency for the current Flyway 9.22.3 baseline.
- Stabilized service and web controller tests for the updated domain/security dependencies.

Remaining:

- Repair or regenerate the Maven wrapper.
- Add or validate Square refund/status calls against Square SDK 40.1.0.20240604.
- Add or validate Braintree refund/status calls against Braintree SDK 3.31.0.
- Add provider contract tests or mocked adapter tests for refund/status/webhook state transitions.
- Add regression tests for merchant tenant enforcement in JWT and API-key requests.

## v1.0.3 - Architecture Alignment: DDD Domain Layer vs Application Layer

Status: implemented.

Priority: high.

The project has a well-designed DDD domain layer (`PaymentIntentAggregate`, `ConnectorAggregate`, `RoutingRuleAggregate`, `Money`, `PaymentStatus`, `RoutingRuleMatcher`, `RoutingDomainService`, `PaymentDomainService`) but the actual business logic in the Application Service layer bypasses it entirely. All payment lifecycle logic, routing matching, refund validation, subscription period calculation, and payout aggregation are implemented directly on JPA entities via setters in Application Services (`PaymentIntentService`, `RoutingEngine`, `RefundService`, `SubscriptionService`, `PayoutService`, etc.). This is a Transaction Script architecture wearing a DDD directory structure.

### Problem Summary

| Component | Domain Layer (unused) | Application Layer (actual) |
|-----------|----------------------|---------------------------|
| Payment state machine | `PaymentIntentAggregate.confirm/capture/cancel` with status guards | `PaymentIntentService` direct `setStatus()` + manual if-checks |
| Routing matching | `RoutingRuleMatcher.matches(RoutingCriteria)` with value objects | `RoutingEngine.matches()` with `Arrays.asList(str.split(","))` |
| Weighted selection | `RoutingDomainService.weightedSelect(ConnectorAggregate)` | `RoutingEngine.weightedSelect(ProviderAccount)` - duplicated logic |
| Domain events | `PaymentSucceededEvent` / `PaymentFailedEvent` emitted by aggregate | `publishIfTerminal()` in Application Service |
| Money operations | `Money.add/subtract/isGreaterThan` value object | `BigInteger` operations scattered in services |

### Required Changes

- Wire Application Services to use domain aggregates and domain services instead of directly manipulating JPA entities.
- Route `PaymentIntentService.confirm()` through `PaymentDomainService` → `PaymentIntentAggregate`.
- Route `RoutingEngine.resolve()` through `RoutingDomainService.resolve()`.
- Move `calculateNextPeriod()` from `SubscriptionService` into a `Subscription` aggregate or domain service.
- Move refund amount validation from `RefundService` into domain logic.
- Align `ProviderAccount` and `ConnectorAggregate` so the routing domain service operates on aggregates rather than JPA entities.
- Remove duplicated matching/selection logic between `RoutingEngine` and `RoutingDomainService`.
- Add domain event publishing through aggregates rather than Application Service helper methods.
- Add tests that verify domain aggregates enforce invariants (state transitions, business rules).

### Verification

- After alignment, `PaymentIntentService` should delegate to domain layer for state transitions rather than calling `setStatus()` directly.
- `RoutingEngine` should be a thin facade over `RoutingDomainService`, not contain its own matching logic.
- All business rule validation (refund amount, subscription period, payment status guards) should live in domain aggregates or domain services.

---

## v1.1.0 - Security and Access Control

Status: implemented.

Priority: high.

- Implement `AdminAuthController` for platform admin login/refresh/logout.
- Implement `AdminJwtFilter` and separate admin JWT claims from merchant JWT claims.
- Replace enum-only role checks with permission-backed RBAC tables.
- Add `permissions`, `roles`, `role_permissions`, and `user_roles` migrations.
- Add permission entities, repositories, services, and seed data.
- Add `@RequirePermission` and an AOP permission check for protected operations.
- Add frontend route guards and permission-aware UI controls.

## v1.2.0 - Billing and Dashboard Completion

Status: implemented.

Priority: medium.

- Add invoice entity, migrations, API, and subscription invoice events.
- Align frontend dashboard routes and API clients for `/customers` and `/subscriptions`.
- Complete refunds and webhook endpoint pages in the merchant dashboard.
- Complete admin pages for merchant management, monitoring, provider configuration, reports, and audit logs.
- Add SMTP-backed email delivery for verification, reset, and notifications.
- Add password reset and MFA backup-code user flows if missing from deployed UI paths.

## v1.3.0 - Quality and Observability

Priority: medium.

- Raise automated test coverage with focused unit and integration tests.
- Add Testcontainers coverage for repository, service, and webhook flows.
- Add Playwright or Cypress E2E coverage for merchant and admin workflows.
- Add Prometheus metrics, Grafana dashboards, and structured logging.
- Add OpenTelemetry tracing for request, provider, and webhook delivery paths.
- Add Redis for distributed rate limiting, sessions/blacklists, and hot config caching.

## v1.4.0 - Provider and Payment Method Expansion

Priority: medium to low.

- Add PayPal provider support.
- Add Adyen and Checkout.com provider support.
- Expand provider-specific capabilities while keeping the provider port stable.
- Add more APM support to the Elements SDK.
- Improve 3DS automation and iframe handoff in the Payment Element.
- Add React and Vue Elements wrappers and publishable package artifacts.

## v1.5.0 - Card Vault

Status: implemented; Java 17 verified.

Priority: high.

Completed in this cycle:
- Added polymorphic vault storage for cards, bank accounts, and wallets.
- Added per-entry AES-256-GCM data keys wrapped by a master+custodian key model.
- Added vault token hashing, merchant-scoped fingerprinting, active-token deduplication, and integrity signatures.
- Avoided storing CVC in encrypted card payloads.
- Added vault audit logs for tokenize, detokenize, read/deduplication, and revoke operations.
- Added authenticated merchant Vault APIs for tokenize, list, detokenize, revoke, and audit log retrieval.
- Replaced public gateway token stubs with real `vault_` token creation through `/pub/tokenize` and `/pub/elements/tokenize`.
- Wired Card/Payment/Setup Elements tokenization paths to the vault endpoint.
- Added focused `VaultServiceTest` coverage for sensitive-field handling, deduplication, tenant isolation, and revoke behavior.

Verification notes:
- `mvn -DskipTests compile` passes with `JAVA_HOME=D:\Java\jdk-17`.
- `mvn -pl nexuspay-service -am -Dtest=VaultServiceTest '-Dsurefire.failIfNoSpecifiedTests=false' test` passes: 4 tests, 0 failures.
- Full backend `mvn test` passes with `JAVA_HOME=D:\Java\jdk-17`: 134 tests, 0 failures.

## v2.0.0 - Enterprise Features

Priority: future.

- Organization-level isolation and tenant administration.
- White-label merchant branding.
- Fee-aware and ML-assisted routing.
- Revenue analytics and provider performance reporting.
- PCI DSS, GDPR, SOC 2, and data-retention readiness work.

## v3.0.0 - Cloud Native

Priority: future.

- Helm charts and Kubernetes deployment assets.
- Autoscaling and blue/green deployment workflows.
- AWS, GCP, and Azure deployment guides.
- Multi-region disaster recovery and backup procedures.

---

## Gap Analysis: NexusPay vs Hyperswitch

Comparison against [Hyperswitch](https://github.com/juspay/hyperswitch) (juspay, Rust, 42.8k stars, 300+ connectors) — the leading open-source payment orchestration platform.

### Feature Gap Summary

| # | Capability | Hyperswitch | NexusPay | Gap |
|---|-----------|-------------|----------|-----|
| 1 | **Card Vault / Locker** | PCI DSS L1 vault, JWS/JWE encryption, master+custodian key model, polymorphic storage | Implemented v1.5.0 vault tokens, envelope encryption, master+custodian key wrapping, audit logs, and polymorphic storage | **Medium** - compliance certification and production hardening remain |
| 2 | **Network Tokenization** | Visa/MC network tokens, 3 flows (payment-time, vault-time, standalone API), cryptogram management | None | **Major** |
| 3 | **Cost Observability** | Granular per-provider/per-method/per-region cost breakdown, invoice auditing, interchange downgrade detection, scheme fee analysis, PSP markup transparency | Basic payout fee calc (2.9% hardcoded) | **Major** |
| 4 | **Revenue Recovery / Smart Retries** | ML-powered, 20+ parameters, 4 retry categories (cascading, step-up, clear PAN, global network), configurable per subscription/PM, error code DB across 100+ processors | Basic exponential backoff, no categorization | **Major** |
| 5 | **DSL-Based Routing** | No-code rule config (BIN/currency/amount/metadata), volume-based %, A/B traffic split, cost-based routing | Basic weighted + priority routing, string-split matching | **Medium** |
| 6 | **Connector Count** | 300+ processors via connector template system | 3 (Stripe, Square, Braintree) | **Major** |
| 7 | **Org Hierarchy & Multi-tenancy** | Organization → Account → Profile model, hierarchical isolation, programmatic onboarding, BYOP (Bring Your Own Processor), MoR + Connected Account models | Flat Organization → Merchant, no hierarchy | **Medium** |
| 8 | **APM Widget** | Embeddable APM widgets, Klarna/WeChat/Alipay/Affirm etc. | Basic Elements SDK skeleton (Card/Payment/ApplePay/GooglePay/Alipay/WeChat) | **Medium** |
| 9 | **3-Way Reconciliation** | 2-way + 3-way, backdated support, staggered scheduling, customizable outputs | Basic 2-way provider status comparison | **Medium** |
| 10 | **Fraud/Risk Integration** | Downstream-of-risk-engine integration pattern | None | **Major** |
| 11 | **Redis Infrastructure** | Redis for caching + job queuing (Scheduler Producer/Consumer) | Not implemented (roadmap mention only) | **Medium** |
| 12 | **Observability Stack** | OTel traces, Prometheus metrics, Loki logs, Tempo tracing, Grafana dashboards — all production-ready | Not implemented (roadmap mention only) | **Medium** |
| 13 | **K8s/Cloud Deployment** | Helm charts, AWS CDK, BYOC self-hosted model | docker-compose only | **Medium** |
| 14 | **Control Center** | Full hosted sandbox, connector config UI, routing rules UI, logs viewer, retry config UI | Separate Vue dashboards (merchant + admin), partial coverage | **Medium** |
| 15 | **PCI DSS / GDPR Compliance** | Built-in: PCI DSS L1 vault, GDPR PII storage, data retention | Not implemented (v2.0.0 roadmap mention) | **Major** |
| 16 | **3DS / SCA** | Automatic 3DS challenge handling, frictionless + challenge flows | Basic 3DS skeleton, action URL field only | **Medium** |

### Priority Roadmap Integration

Based on the gap analysis, the following should be prioritized in future versions:

| Version | New Items from Gap Analysis |
|---------|---------------------------|
| v1.0.3 | Architecture alignment (already defined) |
| v1.1.0 | Security + RBAC (already defined) |
| v1.2.0 | Billing completion (already defined) + **DSL-based routing rules** with volume %, A/B testing |
| v1.3.0 | Quality + observability (already defined) + **Redis infrastructure** (caching, rate limiting, job queues) + **Full OTel/Prometheus/Grafana/Loki/Tempo stack** |
| v1.4.0 | Provider expansion (already defined) + **Connector template system** for rapid provider onboarding |
| **v1.5.0** | **Card Vault**: implemented vault tokens, envelope encryption, master+custodian key wrapping, audit logs, and polymorphic storage for cards/bank accounts/wallets. Java 17 compile, focused VaultServiceTest, and full backend `mvn test` verification completed. |
| **v1.6.0 (new)** | **Smart Retries & Revenue Recovery**: ML-powered retry engine, categorized error handling (cascading/step-up/clear PAN/global network), error code DB, configurable strategies per subscription/payment method |
| **v1.7.0 (new)** | **Cost Observability**: per-provider/per-method/per-region cost breakdown, invoice auditing, interchange downgrade detection, PSP markup transparency |
| **v1.8.0 (new)** | **Network Tokenization**: Visa/MC network token provisioning, 3 integration flows, cryptogram management |
| v2.0.0 | Enterprise (already defined) + **Fraud/Risk engine integration**, **3-way reconciliation**, **PCI/GDPR compliance hardening** |
| v3.0.0 | Cloud native (already defined) + **K8s/Helm/CDK deployment**, **BYOC self-hosted model** |

### What NexusPay Already Has (on par or ahead)

- DDD domain model design (aggregates, value objects, domain services) — Hyperswitch is procedural Rust
- JWT + API Key dual-channel auth with merchant tenant checks
- MFA (TOTP) support
- Outbox pattern for transactional webhook delivery
- Separate merchant dashboard + admin portal (Vue 3)
- Payment Links (`/pub/pay/{token}`)
- 3 frontend projects (dashboard, admin, Elements SDK) vs Hyperswitch's single Control Center
- Java/Spring ecosystem — larger talent pool than Rust for enterprise teams

---

## Immediate Next Steps

1. Repair or regenerate the Maven wrapper.
2. Add provider contract tests for refund/status/webhook state transitions.
3. Add missing auth/provider regression tests.
4. Continue v1.6.0 Smart Retries planning and implementation.
