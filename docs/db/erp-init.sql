-- ============================================================
-- ERP System — Consolidated Database Init Script
-- ============================================================
-- Source of truth: JPA entity definitions in src/main/java.
-- Run once against a fresh PostgreSQL database.
-- ddl-auto=validate in prod will pass against this schema.
-- ============================================================

-- ============================================================
-- SECTION 1: SHARED / CORE TABLES  (canonical owner: Main module)
-- ============================================================

CREATE TABLE companies (
    company_id   SERIAL         PRIMARY KEY,
    company_name VARCHAR(255)   NOT NULL,
    currency     VARCHAR(10)    NOT NULL DEFAULT 'USD',
    tax_default  DECIMAL(5,2)   NOT NULL DEFAULT 0.00,
    locale       VARCHAR(20)    NOT NULL DEFAULT 'en-US',
    is_active    BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE product_categories (
    category_id        SERIAL        PRIMARY KEY,
    category_name      VARCHAR(255)  NOT NULL UNIQUE,
    parent_category_id INT           REFERENCES product_categories(category_id),
    is_active          BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE products (
    product_id      BIGSERIAL      PRIMARY KEY,
    product_name    VARCHAR(255)   NOT NULL,
    sku             VARCHAR(100)   NOT NULL UNIQUE,
    unit_of_measure VARCHAR(50),
    cost_price      DECIMAL(19,4)  NOT NULL DEFAULT 0.00,
    selling_price   DECIMAL(19,4)  NOT NULL DEFAULT 0.00,
    reorder_level   DECIMAL(19,4)  NOT NULL DEFAULT 0.00,
    category_id     BIGINT,
    company_id      BIGINT,
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE business_partners (
    partner_id   BIGSERIAL     PRIMARY KEY,
    partner_name VARCHAR(255)  NOT NULL,
    email        VARCHAR(255)  UNIQUE,
    phone        VARCHAR(50),
    address      VARCHAR(500),
    city         VARCHAR(100),
    country      VARCHAR(100),
    type         VARCHAR(50),
    company_id   BIGINT,
    is_active    BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE warehouses (
    warehouse_id   BIGSERIAL     PRIMARY KEY,
    warehouse_name VARCHAR(255)  NOT NULL,
    address        VARCHAR(500),
    company_id     BIGINT        NOT NULL,
    is_active      BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE inventory_locations (
    location_id        BIGSERIAL      PRIMARY KEY,
    location_name      VARCHAR(100)   NOT NULL,
    warehouse_id       BIGINT         NOT NULL,
    product_id         BIGINT         NOT NULL,
    quantity_on_hand   DECIMAL(19,4)  NOT NULL DEFAULT 0.00,
    quantity_reserved  DECIMAL(19,4)  NOT NULL DEFAULT 0.00,
    quantity_available DECIMAL(19,4)  NOT NULL DEFAULT 0.00
);

CREATE TABLE bulk_imports (
    import_id       BIGSERIAL     PRIMARY KEY,
    company_id      BIGINT        NOT NULL,
    imported_by     BIGINT,
    entity_type     VARCHAR(50)   NOT NULL,
    filename        VARCHAR(255)  NOT NULL,
    total_rows      INT           NOT NULL DEFAULT 0,
    successful_rows INT           NOT NULL DEFAULT 0,
    failed_rows     INT           NOT NULL DEFAULT 0,
    status          VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    imported_at     TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE bulk_import_errors (
    error_id      BIGSERIAL      PRIMARY KEY,
    import_id     BIGINT         NOT NULL,
    row_number    INT,
    field_name    VARCHAR(100),
    error_message VARCHAR(1000)  NOT NULL
);

-- ============================================================
-- SECTION 2: ADMIN MODULE  (RBAC, auth, audit)
-- ============================================================

CREATE TABLE modules (
    module_id   SERIAL      PRIMARY KEY,
    module_name VARCHAR(50) NOT NULL UNIQUE,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE actions (
    action_id   SERIAL      PRIMARY KEY,
    action_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permissions (
    permission_id SERIAL PRIMARY KEY,
    module_id     INT    NOT NULL REFERENCES modules(module_id)  ON DELETE CASCADE,
    action_id     INT    NOT NULL REFERENCES actions(action_id)  ON DELETE CASCADE,
    UNIQUE (module_id, action_id)
);

CREATE TABLE roles (
    role_id     SERIAL       PRIMARY KEY,
    role_name   VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    company_id  INT          NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE (company_id, role_name)
);

CREATE TABLE role_permissions (
    role_id       INT NOT NULL REFERENCES roles(role_id)            ON DELETE CASCADE,
    permission_id INT NOT NULL REFERENCES permissions(permission_id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- users table — consolidated; entity: com.sefinal.erp.entity.User
CREATE TABLE users (
    user_id               BIGSERIAL     PRIMARY KEY,
    first_name            VARCHAR(100)  NOT NULL,
    last_name             VARCHAR(100)  NOT NULL,
    email                 VARCHAR(255)  NOT NULL UNIQUE,
    password_hash         VARCHAR(255)  NOT NULL,
    company_id            BIGINT,
    role_id               BIGINT,
    is_active             BOOLEAN       NOT NULL DEFAULT TRUE,
    mfa_enabled           BOOLEAN       NOT NULL DEFAULT FALSE,
    failed_login_attempts INT           NOT NULL DEFAULT 0,
    locked_until          TIMESTAMP,
    created_at            TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Admin-service audit log (used by Admin service only)
CREATE TABLE audit_log (
    audit_id    BIGSERIAL    PRIMARY KEY,
    user_id     INT          REFERENCES users(user_id) ON DELETE SET NULL,
    company_id  INT          NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    action      VARCHAR(50)  NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   INT,
    details     TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ERP-service audit log: entity ErpAuditLog → erp_audit_logs
CREATE TABLE erp_audit_logs (
    log_id       BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT,
    entity_type  VARCHAR(50)  NOT NULL,
    entity_id    BIGINT,
    action       VARCHAR(30)  NOT NULL,
    old_values   TEXT,
    new_values   TEXT,
    performed_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- entity ReportExport → report_exports
CREATE TABLE report_exports (
    export_id    BIGSERIAL   PRIMARY KEY,
    generated_by BIGINT,
    report_type  VARCHAR(50) NOT NULL,
    period_start DATE,
    period_end   DATE,
    file_format  VARCHAR(10) NOT NULL DEFAULT 'csv',
    file_path    VARCHAR(500),
    generated_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ============================================================
-- SECTION 3: SALES MODULE
-- ============================================================

CREATE TABLE sales_orders (
    sales_order_id     BIGSERIAL     PRIMARY KEY,
    sales_order_number VARCHAR(100)  UNIQUE NOT NULL,
    partner_id         BIGINT        REFERENCES business_partners(partner_id),
    created_by         BIGINT        REFERENCES users(user_id),
    order_date         DATE          DEFAULT CURRENT_DATE,
    subtotal           DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount         DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount       DECIMAL(19,4) NOT NULL DEFAULT 0,
    status             VARCHAR(50)   NOT NULL,
    created_at         TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE sales_order_lines (
    line_id        BIGSERIAL     PRIMARY KEY,
    sales_order_id BIGINT        REFERENCES sales_orders(sales_order_id),
    product_id     BIGINT        REFERENCES products(product_id),
    quantity       DECIMAL(19,4) NOT NULL,
    unit_price     DECIMAL(19,4) NOT NULL,
    line_total     DECIMAL(19,4) NOT NULL
);

CREATE TABLE sales_invoices (
    invoice_id     BIGSERIAL     PRIMARY KEY,
    invoice_number VARCHAR(100)  UNIQUE NOT NULL,
    sales_order_id BIGINT        REFERENCES sales_orders(sales_order_id),
    partner_id     BIGINT        REFERENCES business_partners(partner_id),
    created_by     BIGINT        REFERENCES users(user_id),
    invoice_date   DATE          DEFAULT CURRENT_DATE,
    due_date       DATE,
    subtotal       DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount     DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount   DECIMAL(19,4) NOT NULL DEFAULT 0,
    status         VARCHAR(50)   NOT NULL,
    created_at     TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE sales_invoice_lines (
    line_id    BIGSERIAL     PRIMARY KEY,
    invoice_id BIGINT        REFERENCES sales_invoices(invoice_id),
    product_id BIGINT        REFERENCES products(product_id),
    quantity   DECIMAL(19,4) NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    line_total DECIMAL(19,4) NOT NULL
);

CREATE TABLE payments (
    payment_id     BIGSERIAL     PRIMARY KEY,
    invoice_id     BIGINT        REFERENCES sales_invoices(invoice_id),
    amount         DECIMAL(19,4) NOT NULL,
    payment_date   DATE          DEFAULT CURRENT_DATE,
    payment_method VARCHAR(50),
    reference      VARCHAR(255),
    created_at     TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE approval_requests (
    approval_id  BIGSERIAL   PRIMARY KEY,
    invoice_id   BIGINT      REFERENCES sales_invoices(invoice_id),
    requested_by BIGINT      REFERENCES users(user_id),
    reviewed_by  BIGINT      REFERENCES users(user_id),
    status       VARCHAR(50) DEFAULT 'PENDING',
    comments     TEXT,
    requested_at TIMESTAMP   DEFAULT NOW(),
    reviewed_at  TIMESTAMP
);

CREATE TABLE sales_returns (
    return_id     BIGSERIAL     PRIMARY KEY,
    return_number VARCHAR(100)  UNIQUE NOT NULL,
    invoice_id    BIGINT        REFERENCES sales_invoices(invoice_id),
    processed_by  BIGINT        REFERENCES users(user_id),
    return_date   DATE          DEFAULT CURRENT_DATE,
    reason        TEXT,
    total_amount  DECIMAL(19,4) NOT NULL DEFAULT 0,
    status        VARCHAR(50)   NOT NULL,
    created_at    TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE sales_return_lines (
    return_line_id BIGSERIAL     PRIMARY KEY,
    return_id      BIGINT        REFERENCES sales_returns(return_id),
    product_id     BIGINT        REFERENCES products(product_id),
    quantity       DECIMAL(19,4) NOT NULL,
    unit_price     DECIMAL(19,4) NOT NULL,
    line_total     DECIMAL(19,4) NOT NULL
);

-- ============================================================
-- SECTION 4: PURCHASING & INVENTORY MODULE
-- ============================================================

CREATE TABLE purchase_orders (
    po_id        BIGSERIAL     PRIMARY KEY,
    po_number    VARCHAR(50)   UNIQUE NOT NULL,
    partner_id   BIGINT        NOT NULL,
    created_by   BIGINT,
    order_date   DATE          NOT NULL,
    total_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    status       VARCHAR(30)   NOT NULL DEFAULT 'pending',
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_order_lines (
    line_id          BIGSERIAL     PRIMARY KEY,
    po_id            BIGINT        NOT NULL,
    product_id       BIGINT        NOT NULL,
    quantity_ordered DECIMAL(19,4) NOT NULL,
    unit_cost        DECIMAL(19,4) NOT NULL,
    line_total       DECIMAL(19,4) NOT NULL
);

CREATE TABLE supplier_bills (
    bill_id      BIGSERIAL     PRIMARY KEY,
    bill_number  VARCHAR(50)   UNIQUE NOT NULL,
    partner_id   BIGINT        NOT NULL,
    po_id        BIGINT,
    recorded_by  BIGINT,
    bill_date    DATE          NOT NULL,
    due_date     DATE,
    subtotal     DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount   DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    status       VARCHAR(30)   NOT NULL DEFAULT 'draft',
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE supplier_bill_lines (
    bill_line_id BIGSERIAL     PRIMARY KEY,
    bill_id      BIGINT        NOT NULL,
    product_id   BIGINT,
    quantity     DECIMAL(19,4) NOT NULL,
    unit_cost    DECIMAL(19,4) NOT NULL,
    line_total   DECIMAL(19,4) NOT NULL
);

CREATE TABLE goods_receipts (
    receipt_id   BIGSERIAL     PRIMARY KEY,
    po_id        BIGINT,
    received_by  BIGINT,
    receipt_date DATE          NOT NULL,
    notes        VARCHAR(1000),
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE goods_receipt_lines (
    receipt_line_id   BIGSERIAL     PRIMARY KEY,
    receipt_id        BIGINT        NOT NULL,
    product_id        BIGINT        NOT NULL,
    quantity_received DECIMAL(19,4) NOT NULL
);

CREATE TABLE inventory_counts (
    count_id     BIGSERIAL     PRIMARY KEY,
    count_number VARCHAR(50)   UNIQUE NOT NULL,
    warehouse_id BIGINT,
    counted_by   BIGINT,
    count_date   DATE          NOT NULL,
    status       VARCHAR(30)   NOT NULL DEFAULT 'draft',
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory_count_lines (
    count_line_id     BIGSERIAL     PRIMARY KEY,
    count_id          BIGINT        NOT NULL,
    product_id        BIGINT        NOT NULL,
    system_quantity   DECIMAL(19,4),
    counted_quantity  DECIMAL(19,4),
    variance_quantity DECIMAL(19,4)
);

CREATE TABLE inventory_discrepancies (
    discrepancy_id    BIGSERIAL     PRIMARY KEY,
    count_line_id     BIGINT        NOT NULL,
    recorded_by       BIGINT,
    resolved_by       BIGINT,
    system_quantity   DECIMAL(19,4),
    counted_quantity  DECIMAL(19,4),
    variance_quantity DECIMAL(19,4),
    status            VARCHAR(30)   NOT NULL DEFAULT 'open',
    notes             VARCHAR(500),
    recorded_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    resolved_at       TIMESTAMP
);

CREATE TABLE stock_movements (
    movement_id           BIGSERIAL     PRIMARY KEY,
    product_id            BIGINT        NOT NULL,
    location_id           BIGINT,
    goods_receipt_line_id BIGINT,
    sales_invoice_line_id BIGINT,
    sales_return_line_id  BIGINT,
    discrepancy_id        BIGINT,
    quantity_change       DECIMAL(19,4) NOT NULL,
    reason_code           VARCHAR(50)   NOT NULL,
    reference_type        VARCHAR(50),
    reference_id          BIGINT,
    moved_at              TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ============================================================
-- SECTION 5: FINANCE / ALERTS TABLES
-- ============================================================

-- entity JournalEntry → journal_entries
CREATE TABLE journal_entries (
    journal_entry_id BIGSERIAL     PRIMARY KEY,
    invoice_id       BIGINT,
    posted_by        BIGINT,
    entry_date       DATE          NOT NULL,
    reference_number VARCHAR(50)   UNIQUE NOT NULL,
    status           VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    posted_at        TIMESTAMP
);

-- entity JournalEntryLine → journal_entry_lines
CREATE TABLE journal_entry_lines (
    line_id          BIGSERIAL     PRIMARY KEY,
    journal_entry_id BIGINT        NOT NULL,
    account_code     VARCHAR(20)   NOT NULL,
    debit_amount     DECIMAL(19,4) NOT NULL DEFAULT 0,
    credit_amount    DECIMAL(19,4) NOT NULL DEFAULT 0,
    line_description VARCHAR(500)
);

-- entity LowStockAlert → low_stock_alerts
CREATE TABLE low_stock_alerts (
    alert_id          BIGSERIAL     PRIMARY KEY,
    product_id        BIGINT        NOT NULL,
    quantity_at_alert DECIMAL(19,4) NOT NULL,
    reorder_level     DECIMAL(19,4) NOT NULL,
    is_resolved       BOOLEAN       NOT NULL DEFAULT FALSE,
    resolved_by       BIGINT,
    triggered_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    resolved_at       TIMESTAMP
);

-- entity ReorderSuggestion → reorder_suggestions
CREATE TABLE reorder_suggestions (
    suggestion_id              BIGSERIAL     PRIMARY KEY,
    alert_id                   BIGINT,
    product_id                 BIGINT        NOT NULL,
    reviewed_by                BIGINT,
    current_available_quantity DECIMAL(19,4),
    reorder_level_snapshot     DECIMAL(19,4),
    suggested_quantity         DECIMAL(19,4) NOT NULL,
    status                     VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    suggested_at               TIMESTAMP     NOT NULL DEFAULT NOW(),
    reviewed_at                TIMESTAMP
);

-- entity PaymentReminder → payment_reminders
CREATE TABLE payment_reminders (
    reminder_id       BIGSERIAL     PRIMARY KEY,
    invoice_id        BIGINT        NOT NULL,
    sent_by           BIGINT,
    reminder_type     VARCHAR(30)   NOT NULL,
    channel           VARCHAR(30)   NOT NULL,
    due_date_snapshot DATE,
    sent_at           TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ============================================================
-- SECTION 6: INDEXES
-- ============================================================

-- Shared
CREATE INDEX idx_users_company        ON users(company_id);
CREATE INDEX idx_users_email          ON users(email);
CREATE INDEX idx_users_role           ON users(role_id);
CREATE INDEX idx_products_company     ON products(company_id);
CREATE INDEX idx_products_sku         ON products(sku);
CREATE INDEX idx_partners_company     ON business_partners(company_id);
CREATE INDEX idx_warehouses_company   ON warehouses(company_id);
CREATE INDEX idx_inv_loc_warehouse    ON inventory_locations(warehouse_id);
CREATE INDEX idx_inv_loc_product      ON inventory_locations(product_id);

-- Admin
CREATE INDEX idx_roles_company        ON roles(company_id);
CREATE INDEX idx_audit_company_time   ON audit_log(company_id, created_at DESC);
CREATE INDEX idx_audit_user           ON audit_log(user_id);
CREATE INDEX idx_erp_audit_user       ON erp_audit_logs(user_id);
CREATE INDEX idx_report_exports_user  ON report_exports(generated_by);

-- Sales
CREATE INDEX idx_sales_orders_partner ON sales_orders(partner_id);
CREATE INDEX idx_sales_orders_status  ON sales_orders(status);
CREATE INDEX idx_invoices_order       ON sales_invoices(sales_order_id);
CREATE INDEX idx_invoices_status      ON sales_invoices(status);
CREATE INDEX idx_payments_invoice     ON payments(invoice_id);
CREATE INDEX idx_approvals_invoice    ON approval_requests(invoice_id);
CREATE INDEX idx_returns_invoice      ON sales_returns(invoice_id);

-- Purchasing & Inventory
CREATE INDEX idx_po_partner           ON purchase_orders(partner_id);
CREATE INDEX idx_po_status            ON purchase_orders(status);
CREATE INDEX idx_bills_partner        ON supplier_bills(partner_id);
CREATE INDEX idx_bills_status         ON supplier_bills(status);
CREATE INDEX idx_receipts_po          ON goods_receipts(po_id);
CREATE INDEX idx_inv_counts_warehouse ON inventory_counts(warehouse_id);
CREATE INDEX idx_inv_counts_status    ON inventory_counts(status);
CREATE INDEX idx_stock_product        ON stock_movements(product_id);
CREATE INDEX idx_stock_location       ON stock_movements(location_id);
CREATE INDEX idx_low_stock_product    ON low_stock_alerts(product_id);
CREATE INDEX idx_journal_invoice      ON journal_entries(invoice_id);

-- ============================================================
-- SECTION 7: RBAC SEED DATA  (global catalog — same values as Admin V2)
-- ============================================================

INSERT INTO modules (module_name) VALUES
    ('admin'), ('users'), ('roles'), ('companies'),
    ('inventory'), ('sales'), ('purchases'), ('finance'), ('reports');

INSERT INTO actions (action_name) VALUES
    ('create'), ('read'), ('update'), ('delete'), ('approve'), ('export');

INSERT INTO permissions (module_id, action_id)
SELECT m.module_id, a.action_id FROM modules m CROSS JOIN actions a;
