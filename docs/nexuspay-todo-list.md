# NexusPay Current TODO List

Last updated: 2026-06-07

This document tracks the practical remaining work after the high-priority backend stabilization pass.

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

## High Priority Remaining

| Item | Area | Status | Notes |
|------|------|--------|-------|
| Configure JDK 17 build | Build | Done locally | Local Maven verification passes with `JAVA_HOME=D:\Java\jdk-17`. CI and wrapper cleanup still need follow-up. |
| Restore Maven wrapper | Build | Done | `.mvn/wrapper/` regenerated with Maven 3.9.9. |
| Compile provider adapters | Provider | Done for compile and test | Java 17 compile passes. Added refund/status contract tests (+7) and webhook state transition tests (+13). |
| Full backend test suite | Quality | Done | Root `mvn test` passes: 172 tests, 0 failures. |
| Auth regression tests | Security | Done | MerchantTenantSecurityTest covers JWT cross-merchant access, refresh-token rejection, API-key tenant checks, revoked key handling (+18 tests). |
| Provider refund/status tests | Payments | Done | Added ProviderDispatcher refund/status contract tests (+7) and ProviderWebhookServiceTest (+13). |
| Admin auth API | Admin | Not done | Add admin login, refresh, logout, and claim separation. |
| Admin JWT filter | Admin | Not done | Protect `/api/v1/admin/**` with platform-admin identity. |
| Permission-backed RBAC | Security | Not done | Add permission tables, services, annotation, and AOP checks. |
| Invoice module | Billing | Not done | Add invoice entity, API, and subscription invoice events. |
| Frontend route/API alignment | Frontend | Not done | Recheck customers/subscriptions dashboard routes against backend APIs. |

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

Verified on 2026-06-07 with `JAVA_HOME=D:\Java\jdk-17`:

```bash
mvn -DskipTests compile
mvn -pl nexuspay-service -am -Dtest=VaultServiceTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn test
```

Still pending functional/regression coverage:
- Refunds reach the configured provider and persist provider refund IDs.
- Provider webhooks update local payment status and enqueue outbound events.
- Failed webhook deliveries are retried by outbox processing.
- Subscription renewals create/confirm renewal payment intents.
- Reconciliation reports real local/provider status differences.
- Merchant-scoped endpoints reject cross-merchant JWT/API-key requests.
