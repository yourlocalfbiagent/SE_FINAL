# Admin Module — Database Schema Reference

> **Database**: PostgreSQL (currently `se_final_admin`, single schema)
> **Migration tool**: Flyway (V1–V4)
> **Scope**: Admin module only. Other ERP modules (inventory, sales, etc.) run separate databases today. See [db-migration-rds.md](db-migration-rds.md) for the planned shared AWS RDS migration.

---

## Table Overview

| Table | Description |
|-------|-------------|
| `companies` | Top-level tenant. Every other table is scoped to a `company_id`. |
| `modules` | Global catalog of ERP feature areas (admin, sales, inventory, …). |
| `actions` | Global catalog of verbs (create, read, update, delete, approve, export). |
| `permissions` | Cross-product of `modules × actions`. Shared across all tenants. |
| `roles` | Company-scoped role definitions. |
| `role_permissions` | Which permissions a role grants (many-to-many). |
| `users` | Employees belonging to a company, assigned one role. |
| `audit_log` | Immutable append-only record of every significant action. |
| `report_exports` | Tracks every CSV/report export generated per company. |

---

## Tables

### `companies`
Tenant root. All other rows belong to exactly one company.

| Column | Type | Constraints | Default |
|--------|------|-------------|---------|
| `company_id` | `SERIAL` | PK | auto |
| `company_name` | `VARCHAR(150)` | NOT NULL | — |
| `currency` | `VARCHAR(3)` | NOT NULL | `'USD'` |
| `tax_default` | `DECIMAL(5,2)` | NOT NULL | `0.00` |
| `locale` | `VARCHAR(10)` | NOT NULL | `'en-US'` |
| `is_active` | `BOOLEAN` | NOT NULL | `TRUE` |
| `created_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` |

---

### `modules`
Global (not tenant-scoped). Defines feature areas of the ERP platform.

| Column | Type | Constraints |
|--------|------|-------------|
| `module_id` | `SERIAL` | PK |
| `module_name` | `VARCHAR(50)` | NOT NULL, UNIQUE |
| `is_active` | `BOOLEAN` | NOT NULL, DEFAULT TRUE |

**Seeded values** (V2): `admin`, `users`, `roles`, `companies`, `inventory`, `sales`, `purchases`, `finance`, `reports`

---

### `actions`
Global. Defines verbs that can be performed on any module.

| Column | Type | Constraints |
|--------|------|-------------|
| `action_id` | `SERIAL` | PK |
| `action_name` | `VARCHAR(50)` | NOT NULL, UNIQUE |

**Seeded values** (V2): `create`, `read`, `update`, `delete`, `approve`, `export`

---

### `permissions`
Global. Each row is a unique `(module, action)` pair. No tenant column.

| Column | Type | Constraints |
|--------|------|-------------|
| `permission_id` | `SERIAL` | PK |
| `module_id` | `INT` | NOT NULL, FK → `modules.module_id` ON DELETE CASCADE |
| `action_id` | `INT` | NOT NULL, FK → `actions.action_id` ON DELETE CASCADE |
| — | — | UNIQUE `(module_id, action_id)` |

**Seeded count** (V2): 9 modules × 6 actions = **54 rows**

---

### `roles`
Tenant-scoped. Each company defines its own role names.

| Column | Type | Constraints | Default |
|--------|------|-------------|---------|
| `role_id` | `SERIAL` | PK | auto |
| `role_name` | `VARCHAR(50)` | NOT NULL | — |
| `description` | `VARCHAR(255)` | nullable | — |
| `company_id` | `INT` | NOT NULL, FK → `companies.company_id` ON DELETE CASCADE | — |
| `is_active` | `BOOLEAN` | NOT NULL | `TRUE` |
| — | — | UNIQUE `(company_id, role_name)` | — |

**Indexes**: `idx_roles_company ON roles(company_id)`

---

### `role_permissions`
Junction table. Grants a permission to a role.

| Column | Type | Constraints |
|--------|------|-------------|
| `role_id` | `INT` | NOT NULL, FK → `roles.role_id` ON DELETE CASCADE |
| `permission_id` | `INT` | NOT NULL, FK → `permissions.permission_id` ON DELETE CASCADE |
| — | — | PRIMARY KEY `(role_id, permission_id)` |

---

### `users`
Tenant-scoped. One role per user; role is nullable (role-less = no permissions).

| Column | Type | Constraints | Default |
|--------|------|-------------|---------|
| `user_id` | `SERIAL` | PK | auto |
| `first_name` | `VARCHAR(80)` | NOT NULL | — |
| `last_name` | `VARCHAR(80)` | NOT NULL | — |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE (global) | — |
| `password_hash` | `VARCHAR(255)` | NOT NULL | — |
| `company_id` | `INT` | NOT NULL, FK → `companies.company_id` ON DELETE CASCADE | — |
| `role_id` | `INT` | nullable, FK → `roles.role_id` ON DELETE SET NULL | — |
| `is_active` | `BOOLEAN` | NOT NULL | `TRUE` |
| `mfa_enabled` | `BOOLEAN` | NOT NULL | `FALSE` |
| `failed_login_attempts` | `INT` | NOT NULL | `0` |
| `locked_until` | `TIMESTAMP` | nullable | — |
| `created_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` |

**Indexes**:
- `idx_users_company ON users(company_id)`
- `idx_users_role ON users(role_id)`

**Notes**:
- `email` is globally unique today (single DB). In a shared RDS schema, this constraint must stay global or be scoped to `(company_id, email)` depending on business rules.
- `password_hash` uses BCrypt (spring-security-crypto).
- Lockout: `failed_login_attempts` increments on each bad login; `locked_until` is set to `NOW() + 15 min` after 5 failures.

---

### `audit_log`
Append-only. Never updated or deleted by application code.

| Column | Type | Constraints | Default |
|--------|------|-------------|---------|
| `audit_id` | `BIGSERIAL` | PK | auto |
| `user_id` | `INT` | nullable, FK → `users.user_id` ON DELETE SET NULL | — |
| `company_id` | `INT` | NOT NULL, FK → `companies.company_id` ON DELETE CASCADE | — |
| `action` | `VARCHAR(50)` | NOT NULL | — |
| `entity_type` | `VARCHAR(50)` | NOT NULL | — |
| `entity_id` | `INT` | nullable | — |
| `details` | `TEXT` | nullable | — |
| `created_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` |

**Indexes**:
- `idx_audit_company_time ON audit_log(company_id, created_at DESC)` — primary query path
- `idx_audit_user ON audit_log(user_id)`

**Action string convention**: `<entity_type>.<verb>` e.g. `user.create`, `role.update`, `user.login`, `user.lockout`

---

### `report_exports`
Tracks every export run, for audit and rate-limiting purposes.

| Column | Type | Constraints | Default |
|--------|------|-------------|---------|
| `export_id` | `SERIAL` | PK | auto |
| `company_id` | `INT` | NOT NULL, FK → `companies.company_id` ON DELETE CASCADE | — |
| `generated_by` | `INT` | nullable, FK → `users.user_id` ON DELETE SET NULL | — |
| `report_type` | `VARCHAR(50)` | NOT NULL | — |
| `period_start` | `DATE` | nullable | — |
| `period_end` | `DATE` | nullable | — |
| `file_format` | `VARCHAR(10)` | NOT NULL | `'csv'` |
| `row_count` | `INT` | nullable | — |
| `generated_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` |

**Indexes**: `idx_report_exports_company ON report_exports(company_id, generated_at DESC)`

---

## Entity-Relationship Summary

```
companies ──< roles ──< role_permissions >── permissions >── modules
     │                                                    └── actions
     └──< users >── role_id (→ roles)
     └──< audit_log
     └──< report_exports
```

- `companies` is the tenant root; cascade delete propagates to all tenant-scoped tables.
- `modules`, `actions`, `permissions` are global (no `company_id`) — shared across all tenants.
- `role_permissions` is a pure junction; no extra columns.

---

## Flyway Migration History

| Version | File | What it does |
|---------|------|--------------|
| V1 | `V1__init_admin_schema.sql` | Creates all tables and indexes |
| V2 | `V2__seed_catalogs.sql` | Seeds 9 modules, 6 actions, 54 permissions |
| V3 | `V3__audit_log.sql` | Adds `audit_log` table |
| V4 | `V4__report_exports.sql` | Adds `report_exports` table |
