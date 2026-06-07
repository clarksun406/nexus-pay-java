# NexusPay Elements And Billing Roadmap

Last updated: 2026-06-07

This document tracks the frontend Elements SDK, subscription/billing module, and adjacent admin/dashboard work.

## Implemented Foundations

Elements SDK:
- TypeScript SDK package in `frontend-nexuspay-js`.
- Core SDK, Elements container, base Element abstraction, and postMessage utilities.
- Card Element, Setup Element, Apple Pay Element, Google Pay Element, Alipay Element, and WeChatPay Element foundations.
- Payment Element skeleton.
- Demo iframe pages under `frontend-dashboard/elements`.

Billing backend:
- Customer, PaymentMethod, and Subscription entities.
- Customer and subscription repositories/services/controllers.
- Subscription creation, activation, cancellation, and minimal renewal processing.
- Subscription period rollover for month and year intervals.

Dashboard/admin:
- Merchant dashboard pages for customers and subscriptions exist.
- Admin overview and organization foundations exist.
- Admin overview and monitoring APIs now use repository/provider-backed data instead of mock-only responses.

## Remaining Gaps

High priority:
- Validate all billing and Elements-related backend code with JDK 17 beyond the current compile pass.
- Align dashboard customer/subscription API clients and routes with backend endpoints.
- Complete Payment Element dynamic rendering instead of the current skeleton behavior.
- Add automatic 3DS/SCA handoff in the Payment Element.
- Add invoice entity, migrations, API, and subscription invoice events.
- Add tests for subscription renewal success/failure flows.

Medium priority:
- Add subscription webhook events for renewal, cancellation, payment failure, and invoice status changes.
- Complete merchant dashboard refunds and webhook endpoint pages.
- Add React and Vue Elements wrappers.
- Publish npm/CDN artifacts.
- Add deeper style customization through stable CSS variables.

Lower priority:
- Improve form validation, BIN metadata, and error messages.
- Add address autocomplete.
- Improve mobile touch behavior.
- Add additional APMs such as iDEAL and Bancontact.

## Roadmap

### Phase 1 - Stabilize Billing And Payment Element

- Run Java 17 compile and targeted tests.
- Fix provider SDK or subscription compile issues surfaced by verification.
- Add focused tests for subscription renewals.
- Complete dashboard route/API alignment for customers and subscriptions.
- Complete Payment Element dynamic payment method rendering.
- Add 3DS/SCA iframe handoff.

### Phase 2 - Invoice And Webhook Support

- Add invoice tables and domain entity.
- Add invoice APIs and dashboard views.
- Emit subscription and invoice webhook events through the outbox.
- Add failed-renewal retry and dunning hooks.

### Phase 3 - Framework Wrappers

- Add `@nexuspay/react-elements`.
- Add `@nexuspay/vue-elements`.
- Publish npm package artifacts.
- Add CDN bundle and integration examples.

### Phase 4 - UX And Payment Method Expansion

- Add stronger validation and input diagnostics.
- Add address autocomplete.
- Add more APMs.
- Improve mobile and accessibility behavior.

## Technical Debt

- Payment Element remains a skeleton and needs real dynamic rendering.
- Invoice support is not implemented yet.
- Frontend route/API alignment needs another pass.
- Elements package needs publishable artifacts and wrapper packages.
- Java 17 backend compile now passes locally; broad backend test verification still needs follow-up.
