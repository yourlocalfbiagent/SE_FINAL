-- ============================================================
-- ERP System — Consolidated Database Init Script
-- ============================================================
-- Purpose : Single SQL script for production DB setup on AWS RDS.
--           Run once against a fresh PostgreSQL database.
-- Schema  : All tables in default public schema (or admin_svc — see db-migration-rds.md)
-- Versions collected from:
--   mainmodule  V1__create_erp_schema.sql        (canonical shared tables)
--   Admin       V1–V4 migration files             (RBAC + audit)
--   erp-system  V0__init_sales_module_schema.sql  (sales tables)
--   erp-system  V1__purchasing_inventory.sql       (purchasing tables)
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
    product_id      SERIAL         PRIMARY KEY,
    product_name    VARCHAR(255)   NOT NULL,
    sku             VARCHAR(100)   NOT NULL UNIQUE,
    unit_of_measure VARCHAR(50),
    cost_price      DECIMAL(15,4)  NOT NULL DEFAULT 0.00,
    selling_price   DECIMAL(15,4)  NOT NULL DEFAULT 0.00,
    reorder_level   DECIMAL(15,4)  NOT NULL DEFAULT 0.00,
    category_id     INT            REFERENCES product_categories(category_id),
    company_id      INT            NOT NULL REFERENCES companies(company_id),
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE business_partners (
    partner_id   SERIAL        PRIMARY KEY,
    partner_name VARCHAR(255)  NOT NULL,
    email        VARCHAR(255)  UNIQUE,
    phone        VARCHAR(50),
    address      VARCHAR(500),
    city         VARCHAR(100),
    country      VARCHAR(100),
    type         VARCHAR(50)   NOT NULL,
    company_id   INT           NOT NULL REFERENCES companies(company_id),
    is_active    BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE warehouses (
    warehouse_id   SERIAL        PRIMARY KEY,
    warehouse_name VARCHAR(255)  NOT NULL,
    address        VARCHAR(500),
    company_id     INT           NOT NULL REFERENCES companies(company_id),
    is_active      BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE inventory_locations (
    location_id        SERIAL         PRIMARY KEY,
    location_name      VARCHAR(255)   NOT NULL,
    warehouse_id       INT            NOT NULL REFERENCES warehouses(warehouse_id),
    product_id         INT            NOT NULL REFERENCES products(product_id),
    quantity_on_hand   DECIMAL(15,4)  NOT NULL DEFAULT 0.00,
    quantity_reserved  DECIMAL(15,4)  NOT NULL DEFAULT 0.00,
    quantity_available DECIMAL(15,4)  NOT NULL DEFAULT 0.00
);

CREATE TABLE bulk_imports (
    import_id       SERIAL        PRIMARY KEY,
    company_id      INT           NOT NULL REFERENCES companies(company_id),
    imported_by     INT           NOT NULL,
    entity_type     VARCHAR(100)  NOT NULL,
    filename        VARCHAR(500)  NOT NULL,
    total_rows      INT           NOT NULL DEFAULT 0,
    successful_rows INT           NOT NULL DEFAULT 0,
    failed_rows     INT           NOT NULL DEFAULT 0,
    status          VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    imported_at     TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE bulk_import_errors (
    error_id      SERIAL         PRIMARY KEY,
    import_id     INT            NOT NULL REFERENCES bulk_imports(import_id),
    row_number    INT            NOT NULL,
    field_name    VARCHAR(255),
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

-- users table — consolidated from Admin + Main definitions
CREATE TABLE users (
    user_id               SERIAL        PRIMARY KEY,
    first_name            VARCHAR(100)  NOT NULL,
    last_name             VARCHAR(100)  NOT NULL,
    email                 VARCHAR(255)  NOT NULL UNIQUE,
    password_hash         VARCHAR(255)  NOT NULL,
    company_id            INT           NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    role_id               INT           REFERENCES roles(role_id) ON DELETE SET NULL,
    is_active             BOOLEAN       NOT NULL DEFAULT TRUE,
    mfa_enabled           BOOLEAN       NOT NULL DEFAULT FALSE,
    failed_login_attempts INT           NOT NULL DEFAULT 0,
    locked_until          TIMESTAMP,
    created_at            TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Add deferred FK now that users table exists
ALTER TABLE bulk_imports ADD CONSTRAINT fk_bulk_imports_user
    FOREIGN KEY (imported_by) REFERENCES users(user_id);

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

CREATE TABLE report_exports (
    export_id    SERIAL      PRIMARY KEY,
    company_id   INT         NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    generated_by INT         REFERENCES users(user_id) ON DELETE SET NULL,
    report_type  VARCHAR(50) NOT NULL,
    period_start DATE,
    period_end   DATE,
    file_format  VARCHAR(10) NOT NULL DEFAULT 'csv',
    row_count    INT,
    generated_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ============================================================
-- SECTION 3: SALES MODULE
-- ============================================================

CREATE TABLE sales_orders (
    sales_order_id     SERIAL        PRIMARY KEY,
    sales_order_number VARCHAR(100)  UNIQUE NOT NULL,
    partner_id         INT           REFERENCES business_partners(partner_id),
    created_by         INT           REFERENCES users(user_id),
    order_date         DATE          DEFAULT CURRENT_DATE,
    subtotal           DECIMAL(15,4) NOT NULL DEFAULT 0,
    tax_amount         DECIMAL(15,4) NOT NULL DEFAULT 0,
    total_amount       DECIMAL(15,4) NOT NULL DEFAULT 0,
    status             VARCHAR(50)   NOT NULL,
    created_at         TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE sales_order_lines (
    line_id        SERIAL        PRIMARY KEY,
    sales_order_id INT           REFERENCES sales_orders(sales_order_id),
    product_id     INT           REFERENCES products(product_id),
    quantity       DECIMAL(15,4) NOT NULL,
    unit_price     DECIMAL(15,4) NOT NULL,
    line_total     DECIMAL(15,4) NOT NULL
);

CREATE TABLE sales_invoices (
    invoice_id     SERIAL        PRIMARY KEY,
    invoice_number VARCHAR(100)  UNIQUE NOT NULL,
    sales_order_id INT           REFERENCES sales_orders(sales_order_id),
    partner_id     INT           REFERENCES business_partners(partner_id),
    created_by     INT           REFERENCES users(user_id),
    invoice_date   DATE          DEFAULT CURRENT_DATE,
    due_date       DATE,
    subtotal       DECIMAL(15,4) NOT NULL DEFAULT 0,
    tax_amount     DECIMAL(15,4) NOT NULL DEFAULT 0,
    total_amount   DECIMAL(15,4) NOT NULL DEFAULT 0,
    status         VARCHAR(50)   NOT NULL,
    created_at     TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE sales_invoice_lines (
    line_id    SERIAL        PRIMARY KEY,
    invoice_id INT           REFERENCES sales_invoices(invoice_id),
    product_id INT           REFERENCES products(product_id),
    quantity   DECIMAL(15,4) NOT NULL,
    unit_price DECIMAL(15,4) NOT NULL,
    line_total DECIMAL(15,4) NOT NULL
);

CREATE TABLE payments (
    payment_id     SERIAL        PRIMARY KEY,
    invoice_id     INT           REFERENCES sales_invoices(invoice_id),
    amount         DECIMAL(15,4) NOT NULL,
    payment_date   DATE          DEFAULT CURRENT_DATE,
    payment_method VARCHAR(50),
    reference      VARCHAR(255),
    created_at     TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE approval_requests (
    approval_id  SERIAL      PRIMARY KEY,
    invoice_id   INT         REFERENCES sales_invoices(invoice_id),
    requested_by INT         REFERENCES users(user_id),
    reviewed_by  INT         REFERENCES users(user_id),
    status       VARCHAR(50) DEFAULT 'PENDING',
    comments     TEXT,
    requested_at TIMESTAMP   DEFAULT NOW(),
    reviewed_at  TIMESTAMP
);

CREATE TABLE sales_returns (
    return_id     SERIAL        PRIMARY KEY,
    return_number VARCHAR(100)  UNIQUE NOT NULL,
    invoice_id    INT           REFERENCES sales_invoices(invoice_id),
    processed_by  INT           REFERENCES users(user_id),
    return_date   DATE          DEFAULT CURRENT_DATE,
    reason        TEXT,
    total_amount  DECIMAL(15,4) NOT NULL DEFAULT 0,
    status        VARCHAR(50)   NOT NULL,
    created_at    TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE sales_return_lines (
    return_line_id SERIAL        PRIMARY KEY,
    return_id      INT           REFERENCES sales_returns(return_id),
    product_id     INT           REFERENCES products(product_id),
    quantity       DECIMAL(15,4) NOT NULL,
    unit_price     DECIMAL(15,4) NOT NULL,
    line_total     DECIMAL(15,4) NOT NULL
);

-- ============================================================
-- SECTION 4: PURCHASING & INVENTORY MODULE
-- ============================================================

CREATE TABLE purchase_orders (
    po_id        SERIAL      PRIMARY KEY,
    po_number    VARCHAR(50) UNIQUE NOT NULL,
    partner_id   INT         NOT NULL REFERENCES business_partners(partner_id),
    created_by   INT         NOT NULL REFERENCES users(user_id),
    order_date   DATE        NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    status       VARCHAR(30) NOT NULL DEFAULT 'pending',
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_po_total  CHECK (total_amount >= 0),
    CONSTRAINT chk_po_status CHECK (status IN ('pending','approved','rejected','completed','cancelled'))
);

CREATE TABLE purchase_order_lines (
    line_id          SERIAL        PRIMARY KEY,
    po_id            INT           NOT NULL REFERENCES purchase_orders(po_id) ON DELETE CASCADE,
    product_id       INT           NOT NULL REFERENCES products(product_id),
    quantity_ordered NUMERIC(12,2) NOT NULL,
    unit_cost        NUMERIC(12,2) NOT NULL,
    line_total       NUMERIC(12,2) NOT NULL,
    CONSTRAINT chk_po_line_qty       CHECK (quantity_ordered > 0),
    CONSTRAINT chk_po_line_unit_cost CHECK (unit_cost >= 0),
    CONSTRAINT chk_po_line_total     CHECK (line_total >= 0)
);

CREATE TABLE supplier_bills (
    bill_id      SERIAL      PRIMARY KEY,
    bill_number  VARCHAR(50) UNIQUE NOT NULL,
    partner_id   INT         NOT NULL REFERENCES business_partners(partner_id),
    po_id        INT         REFERENCES purchase_orders(po_id),
    recorded_by  INT         NOT NULL REFERENCES users(user_id),
    bill_date    DATE        NOT NULL,
    due_date     DATE,
    subtotal     NUMERIC(12,2) NOT NULL DEFAULT 0,
    tax_amount   NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    status       VARCHAR(30) NOT NULL DEFAULT 'draft',
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_bill_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_bill_tax      CHECK (tax_amount >= 0),
    CONSTRAINT chk_bill_total    CHECK (total_amount >= 0),
    CONSTRAINT chk_bill_dates    CHECK (due_date IS NULL OR due_date >= bill_date),
    CONSTRAINT chk_bill_status   CHECK (status IN ('draft','issued','partially_paid','paid','cancelled'))
);

CREATE TABLE supplier_bill_lines (
    bill_line_id SERIAL        PRIMARY KEY,
    bill_id      INT           NOT NULL REFERENCES supplier_bills(bill_id) ON DELETE CASCADE,
    product_id   INT           NOT NULL REFERENCES products(product_id),
    quantity     NUMERIC(12,2) NOT NULL,
    unit_cost    NUMERIC(12,2) NOT NULL,
    line_total   NUMERIC(12,2) NOT NULL,
    CONSTRAINT chk_supplier_bill_line_qty       CHECK (quantity > 0),
    CONSTRAINT chk_supplier_bill_line_unit_cost CHECK (unit_cost >= 0),
    CONSTRAINT chk_supplier_bill_line_total     CHECK (line_total >= 0)
);

CREATE TABLE goods_receipts (
    receipt_id     SERIAL      PRIMARY KEY,
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    po_id          INT         NOT NULL REFERENCES purchase_orders(po_id),
    received_by    INT         NOT NULL REFERENCES users(user_id),
    receipt_date   DATE        NOT NULL,
    notes          VARCHAR(255),
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE goods_receipt_lines (
    receipt_line_id   SERIAL        PRIMARY KEY,
    receipt_id        INT           NOT NULL REFERENCES goods_receipts(receipt_id) ON DELETE CASCADE,
    product_id        INT           NOT NULL REFERENCES products(product_id),
    quantity_received NUMERIC(12,2) NOT NULL,
    CONSTRAINT chk_goods_receipt_qty CHECK (quantity_received > 0)
);

CREATE TABLE inventory_counts (
    count_id     SERIAL      PRIMARY KEY,
    count_number VARCHAR(50) UNIQUE NOT NULL,
    warehouse_id INT         NOT NULL REFERENCES warehouses(warehouse_id),
    counted_by   INT         NOT NULL REFERENCES users(user_id),
    count_date   DATE        NOT NULL,
    status       VARCHAR(30) NOT NULL DEFAULT 'draft',
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_inventory_count_status CHECK (status IN ('draft','in_progress','completed','cancelled'))
);

CREATE TABLE inventory_count_lines (
    count_line_id     SERIAL        PRIMARY KEY,
    count_id          INT           NOT NULL REFERENCES inventory_counts(count_id) ON DELETE CASCADE,
    product_id        INT           NOT NULL REFERENCES products(product_id),
    system_quantity   NUMERIC(12,2) NOT NULL,
    counted_quantity  NUMERIC(12,2) NOT NULL,
    variance_quantity NUMERIC(12,2) NOT NULL,
    CONSTRAINT chk_inventory_system_qty  CHECK (system_quantity >= 0),
    CONSTRAINT chk_inventory_counted_qty CHECK (counted_quantity >= 0)
);

CREATE TABLE inventory_discrepancies (
    discrepancy_id    SERIAL        PRIMARY KEY,
    count_line_id     INT           NOT NULL REFERENCES inventory_count_lines(count_line_id) ON DELETE CASCADE,
    recorded_by       INT           NOT NULL REFERENCES users(user_id),
    resolved_by       INT           REFERENCES users(user_id),
    system_quantity   NUMERIC(12,2) NOT NULL,
    counted_quantity  NUMERIC(12,2) NOT NULL,
    variance_quantity NUMERIC(12,2) NOT NULL,
    status            VARCHAR(30)   NOT NULL DEFAULT 'open',
    notes             VARCHAR(255),
    recorded_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    resolved_at       TIMESTAMP,
    CONSTRAINT chk_discrepancy_system_qty    CHECK (system_quantity >= 0),
    CONSTRAINT chk_discrepancy_counted_qty   CHECK (counted_quantity >= 0),
    CONSTRAINT chk_discrepancy_status        CHECK (status IN ('open','resolved','ignored')),
    CONSTRAINT chk_discrepancy_resolved_time CHECK (resolved_at IS NULL OR resolved_at >= recorded_at)
);

CREATE TABLE stock_movements (
    movement_id          SERIAL        PRIMARY KEY,
    product_id           INT           NOT NULL REFERENCES products(product_id),
    location_id          INT           NOT NULL REFERENCES inventory_locations(location_id),
    goods_receipt_line_id INT          REFERENCES goods_receipt_lines(receipt_line_id),
    discrepancy_id       INT           REFERENCES inventory_discrepancies(discrepancy_id),
    quantity_change      NUMERIC(12,2) NOT NULL,
    reason_code          VARCHAR(50)   NOT NULL,
    reference_type       VARCHAR(50),
    reference_id         INT,
    moved_at             TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_stock_quantity_change CHECK (quantity_change <> 0),
    CONSTRAINT chk_stock_reference CHECK (
        goods_receipt_line_id IS NOT NULL
        OR discrepancy_id IS NOT NULL
        OR reference_id IS NOT NULL
    )
);

-- ============================================================
-- SECTION 5: INDEXES
-- ============================================================

-- Shared tables
CREATE INDEX idx_users_company         ON users(company_id);
CREATE INDEX idx_users_email           ON users(email);
CREATE INDEX idx_users_role            ON users(role_id);
CREATE INDEX idx_products_company      ON products(company_id);
CREATE INDEX idx_products_sku          ON products(sku);
CREATE INDEX idx_partners_company      ON business_partners(company_id);
CREATE INDEX idx_warehouses_company    ON warehouses(company_id);
CREATE INDEX idx_inv_loc_warehouse     ON inventory_locations(warehouse_id);
CREATE INDEX idx_inv_loc_product       ON inventory_locations(product_id);

-- Admin
CREATE INDEX idx_roles_company         ON roles(company_id);
CREATE INDEX idx_audit_company_time    ON audit_log(company_id, created_at DESC);
CREATE INDEX idx_audit_user            ON audit_log(user_id);
CREATE INDEX idx_report_exports_company ON report_exports(company_id, generated_at DESC);

-- Sales
CREATE INDEX idx_sales_orders_partner  ON sales_orders(partner_id);
CREATE INDEX idx_sales_orders_status   ON sales_orders(status);
CREATE INDEX idx_invoices_order        ON sales_invoices(sales_order_id);
CREATE INDEX idx_invoices_status       ON sales_invoices(status);
CREATE INDEX idx_payments_invoice      ON payments(invoice_id);
CREATE INDEX idx_approvals_invoice     ON approval_requests(invoice_id);
CREATE INDEX idx_returns_invoice       ON sales_returns(invoice_id);

-- Purchasing
CREATE INDEX idx_purchase_orders_partner_id   ON purchase_orders(partner_id);
CREATE INDEX idx_purchase_orders_status       ON purchase_orders(status);
CREATE INDEX idx_supplier_bills_partner_id    ON supplier_bills(partner_id);
CREATE INDEX idx_supplier_bills_status        ON supplier_bills(status);
CREATE INDEX idx_goods_receipts_po_id         ON goods_receipts(po_id);
CREATE INDEX idx_inventory_counts_warehouse   ON inventory_counts(warehouse_id);
CREATE INDEX idx_inventory_counts_status      ON inventory_counts(status);
CREATE INDEX idx_stock_movements_product_id   ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_location_id  ON stock_movements(location_id);

-- ============================================================
-- SECTION 6: RBAC SEED DATA  (global catalog — same values as Admin V2)
-- ============================================================

INSERT INTO modules (module_name) VALUES
    ('admin'), ('users'), ('roles'), ('companies'),
    ('inventory'), ('sales'), ('purchases'), ('finance'), ('reports');

INSERT INTO actions (action_name) VALUES
    ('create'), ('read'), ('update'), ('delete'), ('approve'), ('export');

INSERT INTO permissions (module_id, action_id)
SELECT m.module_id, a.action_id FROM modules m CROSS JOIN actions a;
