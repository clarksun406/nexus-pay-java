# NexusPay Current TODO List

Last updated: 2026-06-09

This document tracks the practical remaining work after the high-priority backend stabilization and 2026-06-09 high-risk security hardening passes.

## Completed In Current Stabilization

High-priority backend gaps addressed:
- JWT and API-key filters now live in the web module where repository dependencies can be wired correctly.
- Request context now includes user, merchant, role, and API-key metadata where applicable.
- Merchant tenant checks now guard merchant-scoped paths and API-key merchant headers.
- Refresh tokens are rejected for normal request authentication.
- Refund creation now calls the selected provider instead of fabricating a local `re_` ID.
- Provider abstractions now include refund and payment-status lookup operations.
- Stripe, Square, and Braintree adapters have refund/status implementations ready for Java 17 compile verification.
- Stripe and Square payment webhooks can update local payment status.
- Outbox events now carry merchant IDs and payment status in JSON payloads.
- Webhook delivery failures propagate back to outbox processing for retry/failure marking.
- Scheduler now invokes health checks, payout summaries, failed payment retries, subscription renewals, and outbox processing.
- Reconciliation now compares local status to provider status instead of returning no discrepancies.
- Admin overview/monitoring now use repository/provider data instead of hardcoded mock data.
- Subscription renewal processing has a minimal automatic loop and fixed month/year period rollover.
- Merchant-scoped resource ownership is now enforced for connector, routing rule, webhook, API key revoke, payment link, refund, payout, invoice, dispute, and connector health metrics paths.
- Routing now validates connector target/fallback ownership and re-checks connector account lookup against the current merchant.
- API key plaintext persistence has been removed; key creation returns the plaintext key once and list responses expose summaries only.
- Merchant/admin refresh-token flows now require explicit refresh tokens and reject cross-context admin/merchant token reuse.
- Merchant registration and member role updates now sync permission-backed `user_roles` grants.
- Payment confirmation now persists `PROCESSING` and selected connector/provider state before external provider charge.
- Added `V10__security_hardening.sql` for legacy plaintext API key cleanup and weak bootstrap admin removal.

## High Priority Remaining

| Item | Area | Status | Notes |
|------|------|--------|-------|
| Configure JDK 17 build | Build | Done locally | Local Maven verification passes with `JAVA_HOME=D:\Java\jdk-17`. CI and wrapper cleanup still need follow-up. |
| Restore Maven wrapper | Build | Done | `.mvn/wrapper/` regenerated with Maven 3.9.9. |
| Compile provider adapters | Provider | Done for compile and test | Java 17 compile passes. Added refund/status contract tests (+7) and webhook state transition tests (+13). |
| Full backend test suite | Quality | Done | Root `mvn test` passes: 172 tests, 0 failures. |
| Auth regression tests | Security | Done | MerchantTenantSecurityTest covers JWT cross-merchant access, refresh-token rejection, API-key tenant checks, revoked key handling (+18 tests). |
| Provider refund/status tests | Payments | Done | Added ProviderDispatcher refund/status contract tests (+7) and ProviderWebhookServiceTest (+13). |
| Admin auth API | Admin | Done | Admin login, refresh, logout, and claim separation exist; refresh now re-checks admin access. |
| Admin JWT filter | Admin | Done | `/api/v1/admin/**` is protected with platform-admin identity. |
| Permission-backed RBAC | Security | Done baseline | Permission tables, services, annotation, AOP checks, and merchant member grant sync exist. Broader endpoint annotation coverage remains a follow-up. |
| Invoice module | Billing | Done | Invoice entity, repository, service, and controller exist; tenant-scoped invoice reads/voids are enforced. |
| Frontend route/API alignment | Frontend | Partial | Customers/subscriptions and main dashboard routes exist, but E2E coverage is still pending. |
| High-risk tenant hardening | Security | Done | Merchant-scoped service/controller paths and routing connector ownership checks added on 2026-06-09. |
| API key secret hardening | Security | Done | Plaintext API key storage removed; legacy column cleanup added in V10 migration. |

## Medium Priority Remaining

| Item | Area | Notes |
|------|------|-------|
| SMTP email service | Notifications | Verification, reset, and operational notifications. |
| Merchant refunds page completion | Frontend | Replace skeletons with real API-backed workflows. |
| Merchant webhook page completion | Frontend | Endpoint CRUD, delivery status, and retry views. |
| Admin merchant management page | Admin | Dedicated merchant list/detail/actions. |
| Admin monitoring page | Admin | Charts and provider health details. |
| Admin reports/export | Admin | CSV/Excel and aggregate reporting. |
| Subscription webhook events | Billing | Notify renewals, failures, cancellations, and invoice status. |
| Testcontainers | Quality | Done | RepositoryIntegrationTest (11 tests), @Disabled pending Docker. |
| E2E tests | Quality | Not done | Merchant dashboard, admin, payment links, and checkout flows. |
| CI Testcontainers run | Quality | Not done | Enable Docker-backed repository integration tests in CI instead of relying on skipped local runs. |

## Lower Priority / Future

| Item | Area | Status |
|------|------|--------|
| OpenTelemetry tracing | Observability | Done |
| Prometheus metrics and Grafana dashboards | Observability | Done |
| PayPal provider | Provider |
| Adyen provider | Provider |
| Checkout.com provider | Provider |
| React Elements wrapper | SDK |
| Vue Elements wrapper | SDK |
| Published npm/CDN artifacts | SDK |
| Address autocomplete and additional APMs | SDK |
| Enterprise analytics and white-label features | Enterprise |
| Kubernetes and cloud deployment assets | Cloud |

## Verification Checklist

Verified on 2026-06-09:

```bash
mvn -pl nexuspay-service -am test
mvn test
```

Latest results:
- `mvn -pl nexuspay-service -am test`: 113 service tests, 0 failures.
- Root `mvn test`: build success across all modules; 11 Testcontainers repository integration tests skipped unless Docker is available.

Still pending functional/regression coverage:
- Browser-level E2E tests for merchant dashboard, admin, payment links, and checkout flows.
- Docker-backed repository integration tests in CI.
- Broader permission annotation coverage audit across all protected endpoints.
