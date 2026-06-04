# NexusPay RBAC Design

Last updated: 2026-06-04

## Current State

Implemented today:
- Merchant JWT and API-key request filters enforce access-token/API-key authentication.
- Merchant-scoped paths are checked against the authenticated merchant context.
- Request attributes expose user ID, merchant ID, merchant role, and API-key ID where applicable.

Still missing:
- Platform admin login and admin JWT authentication.
- Permission-backed RBAC tables.
- Permission services and annotation/AOP checks.
- Frontend permission guards and permission-aware rendering.

The current merchant role model is enum based through `MerchantUser.Role` values such as OWNER, ADMIN, DEVELOPER, FINANCE, and VIEWER. That is enough for coarse access, but not enough for precise operation-level authorization.

## Target Model

```text
User
  -> UserRole
       scopeType: MERCHANT | ORGANIZATION | SYSTEM
       scopeId: merchantId | organizationId | null
       roleId
Role
  -> RolePermission
Permission
```

## Proposed Tables

| Table | Purpose |
|-------|---------|
| `permissions` | Permission catalog, for example `PAYMENT_REFUND` or `SYSTEM_MONITOR`. |
| `roles` | Named merchant, organization, and system roles. |
| `role_permissions` | Role-to-permission mapping. |
| `user_roles` | User role grants scoped to merchant, organization, or system. |

Planned migration: `V6__create_rbac_tables.sql`.

## Permission Catalog Draft

Merchant permissions:
- `MERCHANT_READ`
- `MERCHANT_WRITE`
- `PAYMENT_READ`
- `PAYMENT_CREATE`
- `PAYMENT_CAPTURE`
- `PAYMENT_CANCEL`
- `PAYMENT_REFUND`
- `CONNECTOR_MANAGE`
- `ROUTING_RULE_MANAGE`
- `API_KEY_MANAGE`
- `WEBHOOK_MANAGE`
- `MEMBER_MANAGE`
- `CUSTOMER_MANAGE`
- `SUBSCRIPTION_MANAGE`

Platform/admin permissions:
- `ORG_READ`
- `ORG_MANAGE`
- `MERCHANT_APPROVE`
- `MERCHANT_SUSPEND`
- `SYSTEM_MONITOR`
- `SYSTEM_CONFIG`
- `AUDIT_LOG_READ`
- `REPORT_READ`
- `REPORT_EXPORT`

## Default Role Mapping

| Role | Suggested Permissions |
|------|-----------------------|
| `MERCHANT_OWNER` | All merchant permissions, member management, API key management. |
| `MERCHANT_ADMIN` | Merchant configuration, connectors, routing rules, webhooks, customers, subscriptions. |
| `MERCHANT_DEVELOPER` | Payment read/create, API keys, webhooks, test-mode operations. |
| `MERCHANT_FINANCE` | Payment read, refunds, disputes, payouts, reports. |
| `MERCHANT_VIEWER` | Read-only merchant access. |
| `ORG_OWNER` | All organization and platform admin permissions in organization scope. |
| `ORG_ADMIN` | Merchant approval, monitoring, organization read/manage. |
| `ORG_MEMBER` | Read-only organization/admin access. |

## Backend Work Plan

High priority:
- Add RBAC migration and seed data.
- Add `Permission`, `Role`, and `UserRole` entities.
- Add repositories for permission lookup and user grants.
- Add `PermissionService` for merchant/admin permission checks.
- Add `@RequirePermission` annotation.
- Add `PermissionAspect` to enforce operation-level permissions.
- Add admin auth API and admin JWT filter.
- Separate merchant JWT claims from admin JWT claims.

Medium priority:
- Add role management API.
- Add permission bootstrap and migration from existing enum roles.
- Add `/api/v1/merchants/{id}/me/permissions`.
- Add `/api/v1/admin/me/permissions`.
- Add audit logging for permission and role changes.

## Frontend Work Plan

- Add admin login page and auth store.
- Add merchant and admin route guards.
- Add `v-permission` directive.
- Add `usePermission` helper.
- Hide or disable privileged actions when the current user lacks permission.

## Example API Shape

```text
POST /api/v1/admin/auth/login
POST /api/v1/admin/auth/refresh
POST /api/v1/admin/auth/logout

GET /api/v1/admin/me/permissions
GET /api/v1/merchants/{merchantId}/me/permissions
```

## Open Risks

- Existing enum roles need a deterministic migration path into seeded roles.
- Permission checks must preserve the tenant checks already implemented in request filters.
- Admin auth must not reuse merchant JWT claims without a clear audience/scope distinction.
- Tests need to cover both positive permission grants and cross-tenant denial cases.
