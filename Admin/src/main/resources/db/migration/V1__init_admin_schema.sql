-- Admin module: multi-tenant RBAC schema
-- Tenants
CREATE TABLE companies (
    company_id    SERIAL PRIMARY KEY,
    company_name  VARCHAR(150) NOT NULL,
    currency      VARCHAR(3)   NOT NULL DEFAULT 'USD',
    tax_default   DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    locale        VARCHAR(10)  NOT NULL DEFAULT 'en-US',
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Global catalog: feature areas of the ERP (sales, inventory, hr, ...)
CREATE TABLE modules (
    module_id    SERIAL PRIMARY KEY,
    module_name  VARCHAR(50) NOT NULL UNIQUE,
    is_active    BOOLEAN     NOT NULL DEFAULT TRUE
);

-- Global catalog: verbs that can be performed (create, read, update, delete, ...)
CREATE TABLE actions (
    action_id    SERIAL PRIMARY KEY,
    action_name  VARCHAR(50) NOT NULL UNIQUE
);

-- A permission is a (module, action) pair, e.g. (inventory, update)
CREATE TABLE permissions (
    permission_id  SERIAL PRIMARY KEY,
    module_id      INT NOT NULL REFERENCES modules(module_id) ON DELETE CASCADE,
    action_id      INT NOT NULL REFERENCES actions(action_id) ON DELETE CASCADE,
    UNIQUE (module_id, action_id)
);

-- Roles are scoped per company (every tenant defines its own role names)
CREATE TABLE roles (
    role_id      SERIAL PRIMARY KEY,
    role_name    VARCHAR(50)  NOT NULL,
    description  VARCHAR(255),
    company_id   INT          NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE (company_id, role_name)
);

-- Many-to-many: which permissions does a role grant
CREATE TABLE role_permissions (
    role_id        INT NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    permission_id  INT NOT NULL REFERENCES permissions(permission_id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Users belong to one company and have one role
CREATE TABLE users (
    user_id               SERIAL PRIMARY KEY,
    first_name            VARCHAR(80)  NOT NULL,
    last_name             VARCHAR(80)  NOT NULL,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    company_id            INT          NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    role_id               INT          REFERENCES roles(role_id) ON DELETE SET NULL,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    mfa_enabled           BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    locked_until          TIMESTAMP,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_company   ON users (company_id);
CREATE INDEX idx_users_role      ON users (role_id);
CREATE INDEX idx_roles_company   ON roles (company_id);
