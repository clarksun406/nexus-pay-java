# NexusPay Roadmap

Last updated: 2026-06-04

## Current Status

NexusPay Java is in a v1 stabilization phase. The core backend modules, provider adapter surface, dashboard APIs, Elements SDK skeleton, and admin foundations exist, but the project should not be treated as fully production-ready until the remaining verification and security gaps are closed.

The current high-priority stabilization work has been implemented in code and is ready for a Java 17 compile/test pass.

## v1.0.1 - High-Priority Stabilization

Status: implemented, pending Java 17 verification.

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
- Local Maven currently runs with `D:\Java\jdk1.8.0_202`, but this project requires Java 17.
- `mvn -DskipTests compile` cannot complete under the current Java 8 environment.
- `mvnw.cmd` exists, but the Maven wrapper support directory is missing, so use system Maven after configuring JDK 17.
- Square and Braintree adapter method signatures still need a compile pass with their SDK jars available.

## v1.0.2 - Build and Provider Verification

Priority: high.

- Configure local and CI builds to use JDK 17.
- Repair or regenerate the Maven wrapper.
- Run full backend compile and test suites.
- Validate Square refund/status calls against Square SDK 40.1.0.
- Validate Braintree refund/status calls against Braintree SDK 3.31.0.
- Add provider contract tests or mocked adapter tests for refund/status/webhook state transitions.
- Add regression tests for merchant tenant enforcement in JWT and API-key requests.

## v1.1.0 - Security and Access Control

Priority: high.

- Implement `AdminAuthController` for platform admin login/refresh/logout.
- Implement `AdminJwtFilter` and separate admin JWT claims from merchant JWT claims.
- Replace enum-only role checks with permission-backed RBAC tables.
- Add `permissions`, `roles`, `role_permissions`, and `user_roles` migrations.
- Add permission entities, repositories, services, and seed data.
- Add `@RequirePermission` and an AOP permission check for protected operations.
- Add frontend route guards and permission-aware UI controls.

## v1.2.0 - Billing and Dashboard Completion

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

## Immediate Next Steps

1. Switch local build/runtime to JDK 17.
2. Run `mvn -DskipTests compile`.
3. Fix any SDK signature issues surfaced by the compile pass.
4. Run service tests and add missing auth/provider regression tests.
5. Continue with admin auth and permission-backed RBAC.
