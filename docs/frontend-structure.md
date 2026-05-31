# Frontend Structure Guide

This repository has **three** frontend-related projects:

## 1) `frontend-dashboard/` (Merchant Dashboard)
Main merchant-facing web app.

- Framework: Vue 3 + Vite + Pinia
- Entry: `frontend-dashboard/src/main.ts`
- Router: `frontend-dashboard/src/router/index.ts`
- API client: `frontend-dashboard/src/lib/api.ts`
- Pages: `frontend-dashboard/src/pages/*`

## 2) `frontend-admin/` (Platform Admin Console)
Separate app for platform operations.

- Framework: Vue 3 + Vite + Pinia
- Entry: `frontend-admin/src/main.ts`
- Router: `frontend-admin/src/router/index.ts`
- API client: `frontend-admin/src/lib/api.ts`
- Pages: `frontend-admin/src/pages/*`

Current enabled routes:
- `/admin` -> Overview
- `/admin/organizations` -> Organizations

## 3) `frontend-nexuspay-js/` (Elements SDK)
Embeddable payment SDK (not a dashboard app).

- Framework: TypeScript + Vite (library mode)
- Entry: `frontend-nexuspay-js/src/index.ts`
- Core: `frontend-nexuspay-js/src/core/*`
- Elements: `frontend-nexuspay-js/src/elements/*`

## Notes
- `frontend-dashboard/elements/` contains HTML demos for SDK iframe pages.
- To avoid duplication, admin pages were removed from `frontend-dashboard/src/pages/admin`.
- Admin pages are now maintained only in `frontend-admin/src/pages`.


