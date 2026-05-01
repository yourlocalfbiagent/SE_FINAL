
-- -------------------------------------------------------------
-- 1. COMPANIES
-- -------------------------------------------------------------
CREATE TABLE companies (
                           company_id   SERIAL         PRIMARY KEY,
                           company_name VARCHAR(255)   NOT NULL,
                           currency     VARCHAR(10)    NOT NULL,
                           tax_default  DECIMAL(5, 2)  NOT NULL DEFAULT 0.00,
                           locale       VARCHAR(20),
                           is_active    BOOLEAN        NOT NULL DEFAULT TRUE,
                           created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- 2. USERS  (shared table — owned by Main, referenced everywhere)
-- -------------------------------------------------------------
CREATE TABLE users (
                       user_id               SERIAL        PRIMARY KEY,
                       first_name            VARCHAR(100)  NOT NULL,
                       last_name             VARCHAR(100)  NOT NULL,
                       email                 VARCHAR(255)  NOT NULL UNIQUE,
                       password_hash         VARCHAR(255)  NOT NULL,
                       company_id            INT           NOT NULL REFERENCES companies(company_id),
                       role_id               INT,          -- FK to ROLES (Admin module) — added by Admin migration or on merge
                       is_active             BOOLEAN       NOT NULL DEFAULT TRUE,
                       mfa_enabled           BOOLEAN       NOT NULL DEFAULT FALSE,
                       failed_login_attempts INT           NOT NULL DEFAULT 0,
                       locked_until          TIMESTAMP,
                       created_at            TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- 3. PRODUCT_CATEGORIES  (self-referencing hierarchy)
-- -------------------------------------------------------------
CREATE TABLE product_categories (
                                    category_id        SERIAL        PRIMARY KEY,
                                    category_name      VARCHAR(255)  NOT NULL UNIQUE,
                                    parent_category_id INT           REFERENCES product_categories(category_id),
                                    is_active          BOOLEAN       NOT NULL DEFAULT TRUE
);

-- -------------------------------------------------------------
-- 4. PRODUCTS
-- -------------------------------------------------------------
CREATE TABLE products (
                          product_id    SERIAL         PRIMARY KEY,
                          product_name  VARCHAR(255)   NOT NULL,
                          sku           VARCHAR(100)   NOT NULL UNIQUE,
                          unit_of_measure VARCHAR(50),
                          cost_price    DECIMAL(15, 4) NOT NULL DEFAULT 0.00,
                          selling_price DECIMAL(15, 4) NOT NULL DEFAULT 0.00,
                          reorder_level DECIMAL(15, 4) NOT NULL DEFAULT 0.00,
                          category_id   INT            REFERENCES product_categories(category_id),
                          company_id    INT            NOT NULL REFERENCES companies(company_id),
                          is_active     BOOLEAN        NOT NULL DEFAULT TRUE,
                          created_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- 5. BUSINESS_PARTNERS  (customers & suppliers)
-- -------------------------------------------------------------
CREATE TABLE business_partners (
                                   partner_id   SERIAL        PRIMARY KEY,
                                   partner_name VARCHAR(255)  NOT NULL,
                                   email        VARCHAR(255)  UNIQUE,
                                   phone        VARCHAR(50),
                                   address      VARCHAR(500),
                                   city         VARCHAR(100),
                                   country      VARCHAR(100),
                                   type         VARCHAR(50)   NOT NULL,   -- e.g. 'CUSTOMER', 'SUPPLIER', 'BOTH'
                                   company_id   INT           NOT NULL REFERENCES companies(company_id),
                                   is_active    BOOLEAN       NOT NULL DEFAULT TRUE,
                                   created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- 6. WAREHOUSES
-- -------------------------------------------------------------
CREATE TABLE warehouses (
                            warehouse_id   SERIAL        PRIMARY KEY,
                            warehouse_name VARCHAR(255)  NOT NULL,
                            address        VARCHAR(500),
                            company_id     INT           NOT NULL REFERENCES companies(company_id),
                            is_active      BOOLEAN       NOT NULL DEFAULT TRUE
);

-- -------------------------------------------------------------
-- 7. INVENTORY_LOCATIONS  (bins/shelves inside a warehouse)
-- -------------------------------------------------------------
CREATE TABLE inventory_locations (
                                     location_id        SERIAL         PRIMARY KEY,
                                     location_name      VARCHAR(255)   NOT NULL,
                                     warehouse_id       INT            NOT NULL REFERENCES warehouses(warehouse_id),
                                     product_id         INT            NOT NULL REFERENCES products(product_id),
                                     quantity_on_hand   DECIMAL(15, 4) NOT NULL DEFAULT 0.00,
                                     quantity_reserved  DECIMAL(15, 4) NOT NULL DEFAULT 0.00,
                                     quantity_available DECIMAL(15, 4) NOT NULL DEFAULT 0.00
);

-- -------------------------------------------------------------
-- 8. BULK_IMPORTS
-- -------------------------------------------------------------
CREATE TABLE bulk_imports (
                              import_id       SERIAL        PRIMARY KEY,
                              company_id      INT           NOT NULL REFERENCES companies(company_id),
                              imported_by     INT           NOT NULL REFERENCES users(user_id),
                              entity_type     VARCHAR(100)  NOT NULL,   -- e.g. 'PRODUCT', 'PARTNER'
                              filename        VARCHAR(500)  NOT NULL,
                              total_rows      INT           NOT NULL DEFAULT 0,
                              successful_rows INT           NOT NULL DEFAULT 0,
                              failed_rows     INT           NOT NULL DEFAULT 0,
                              status          VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
                              imported_at     TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- 9. BULK_IMPORT_ERRORS
-- -------------------------------------------------------------
CREATE TABLE bulk_import_errors (
                                    error_id      SERIAL        PRIMARY KEY,
                                    import_id     INT           NOT NULL REFERENCES bulk_imports(import_id),
                                    row_number    INT           NOT NULL,
                                    field_name    VARCHAR(255),
                                    error_message VARCHAR(1000) NOT NULL
);

-- =============================================================
-- INDEXES
-- =============================================================
CREATE INDEX idx_products_company      ON products(company_id);
CREATE INDEX idx_products_category     ON products(category_id);
CREATE INDEX idx_products_sku          ON products(sku);
CREATE INDEX idx_users_company         ON users(company_id);
CREATE INDEX idx_users_email           ON users(email);
CREATE INDEX idx_partners_company      ON business_partners(company_id);
CREATE INDEX idx_warehouses_company    ON warehouses(company_id);
CREATE INDEX idx_inv_loc_warehouse     ON inventory_locations(warehouse_id);
CREATE INDEX idx_inv_loc_product       ON inventory_locations(product_id);
CREATE INDEX idx_bulk_imports_company  ON bulk_imports(company_id);
CREATE INDEX idx_bulk_errors_import    ON bulk_import_errors(import_id);
