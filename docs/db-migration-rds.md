# AWS RDS Migration Notes — Shared Database

> **Context**: Admin module currently runs its own PostgreSQL DB (`se_final_admin`).
> The plan is to ditch per-module Flyway schemas and move all ERP modules into a single
> shared AWS RDS PostgreSQL instance. This document captures what the Admin module owns,
> what is shared/global, and the decisions the team needs to make before creating a unified schema.

---

## What Admin Owns vs What Is Shared

### Global tables (shared with all modules)

These tables are not tenant-specific and every other module will need to read them:

| Table | Owner | Why shared |
|-------|-------|------------|
| `companies` | Admin | Tenant root — every module needs `company_id` |
| `modules` | Admin | Catalog of ERP feature areas — other modules register themselves here |
| `actions` | Admin | Verb catalog for RBAC |
| `permissions` | Admin | `modules × actions` cross-product |
| `roles` | Admin | Company-scoped role definitions |
| `role_permissions` | Admin | Role → permission grants |
| `users` | Admin | Employees; every module needs `user_id` + `company_id` |

### Admin-private tables

Only the Admin module reads/writes these:

| Table | Notes |
|-------|-------|
| `audit_log` | Admin writes; could be shared read-only for other modules' audit queries |
| `report_exports` | Admin-private; tracks admin report runs only |

---

## Key Decisions Before Migration

### 1. Schema namespacing
All current Admin tables use no schema prefix (public schema). On a shared RDS instance,
recommend putting Admin-owned tables in a dedicated schema to avoid name collisions:

```sql
CREATE SCHEMA admin_svc;
-- move tables: companies, modules, actions, permissions, roles, role_permissions, users, audit_log, report_exports
```

Other modules would reference cross-schema: `admin_svc.users`, `admin_svc.companies`, etc.

### 2. `users.email` uniqueness
Currently `email` is globally unique across all companies (`UNIQUE` on `users.email`).
Decide before migration whether:
- **Option A** (keep): One email = one user across all tenants. Simpler, prevents account sharing abuse.
- **Option B** (relax): `UNIQUE(company_id, email)`. Allows same person to have accounts at multiple tenants.

**Recommendation**: Keep global uniqueness. Change only if a real business case arises.

### 3. Remove Flyway per-module, use shared migrations
Current setup: each module has its own Flyway baseline. On shared RDS:
- Create one migration project (or a `db-migrations` repo/folder)
- Single ordered migration history applies to the shared schema
- Modules no longer own their own `db/migration/` folder
- Admin tables should be in the earliest migrations (V1–V4) since other modules depend on them

### 4. `SERIAL` vs `BIGSERIAL` vs `UUID`
Current Admin PKs use `SERIAL` (32-bit int). For a shared multi-module RDS instance,
consider switching to `BIGSERIAL` (64-bit) or UUIDs to avoid PK exhaustion and
inter-module joins being ambiguous:

| Current | Recommendation |
|---------|---------------|
| `SERIAL` (users, roles, etc.) | `BIGSERIAL` or `UUID` |
| `BIGSERIAL` (audit_log) | Keep or UUID |

### 5. Cross-module FK references
Other modules (inventory, sales, purchases, finance) will have foreign keys into Admin tables.
They should reference `admin_svc.users(user_id)` and `admin_svc.companies(company_id)`.
Use `ON DELETE RESTRICT` or `ON DELETE SET NULL` depending on business logic per module.
Do NOT use `CASCADE` from non-admin modules into Admin tables.

### 6. `audit_log` — shared or per-module?
Currently Admin's `audit_log` covers only Admin entity actions. Options:
- **Option A**: Keep as Admin-only; each module has its own audit table.
- **Option B**: Make `audit_log` shared — add a `module` column, all modules write to it.

**Option B** is cleaner for compliance reporting but requires Admin team to maintain the schema on behalf of all modules.

### 7. `report_exports` — Admin-private, no changes needed
No other module references this table. Rename to `admin_report_exports` for clarity in shared schema.

---

## Proposed Shared Schema Checklist

- [ ] Create `admin_svc` schema on RDS
- [ ] Move V1–V4 DDL into shared migration project as first migrations
- [ ] Decide `email` uniqueness scope
- [ ] Decide `SERIAL` vs `BIGSERIAL` / UUID for PKs
- [ ] Decide shared vs per-module `audit_log`
- [ ] Rename `report_exports` → `admin_report_exports`
- [ ] Grant `SELECT` on `admin_svc.users`, `admin_svc.companies` to other module DB users
- [ ] Grant `INSERT` on `admin_svc.audit_log` if going shared audit approach
- [ ] Update Admin service `application.properties` to point at RDS endpoint and `admin_svc` schema
- [ ] Drop per-module Flyway config from `Admin/pom.xml` and `Admin/src/main/resources/db/migration/`
- [ ] Remove `spring.flyway.*` properties from `application.properties`

---

## Current Connection Config (local dev)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/se_final_admin
spring.datasource.username=${DB_USER:ahmad}
spring.datasource.password=${DB_PASSWORD:1234}
```

**RDS target config** (to be updated):
```properties
spring.datasource.url=jdbc:postgresql://<rds-endpoint>:5432/se_final?currentSchema=admin_svc
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

---

## Tables Other Modules Will Likely Read

| Table | Columns other modules care about |
|-------|----------------------------------|
| `companies` | `company_id`, `company_name`, `currency`, `locale`, `is_active` |
| `users` | `user_id`, `company_id`, `email`, `first_name`, `last_name`, `role_id`, `is_active` |
| `roles` | `role_id`, `company_id`, `role_name`, `is_active` |
| `permissions` | `permission_id`, `module_id`, `action_id` |
| `role_permissions` | `role_id`, `permission_id` |
| `modules` | `module_id`, `module_name` |
| `actions` | `action_id`, `action_name` |

Columns like `password_hash`, `failed_login_attempts`, `locked_until` are Admin-internal and
other modules should never need them.
