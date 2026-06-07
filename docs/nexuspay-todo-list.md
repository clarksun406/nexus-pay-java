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
| Restore Maven wrapper | Build | Not done | `mvnw.cmd` exists, but `.mvn/wrapper` is missing. |
| Compile provider adapters | Provider | Done for compile | Java 17 compile passes after correcting SDK coordinates and Square create-payment builder usage. Add provider contract tests next. |
| Full backend test suite | Quality | Not done | Focused `VaultServiceTest` passes; broad `mvn test` still needs a dedicated cleanup pass. |
| Auth regression tests | Security | Not done | Cover JWT access-token enforcement, merchant path checks, and API-key tenant checks. |
| Provider refund/status tests | Payments | Not done | Add Stripe/Square/Braintree adapter or contract tests. |
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
| Testcontainers | Quality | Database, repository, webhook, and scheduler integration tests. |
| E2E tests | Quality | Merchant dashboard, admin, payment links, and checkout flows. |

## Lower Priority / Future

| Item | Area |
|------|------|
| Redis distributed rate limiting and cache | Performance |
| OpenTelemetry tracing | Observability |
| Prometheus metrics and Grafana dashboards | Observability |
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
```

Still pending:

```bash
mvn test
```

Then verify:
- Refunds reach the configured provider and persist provider refund IDs.
- Provider webhooks update local payment status and enqueue outbound events.
- Failed webhook deliveries are retried by outbox processing.
- Subscription renewals create/confirm renewal payment intents.
- Reconciliation reports real local/provider status differences.
- Merchant-scoped endpoints reject cross-merchant JWT/API-key requests.
