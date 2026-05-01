-- ERP module: inventory, products, partners, imports
-- FIRST: companies
CREATE TABLE companies (
                           company_id    SERIAL PRIMARY KEY,
                           company_name  VARCHAR(150) NOT NULL,
                           currency      VARCHAR(3)   NOT NULL DEFAULT 'USD',
                           tax_default   DECIMAL(5,2) NOT NULL DEFAULT 0.00,
                           locale        VARCHAR(10)  NOT NULL DEFAULT 'en-US',
                           is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
                           created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- Product categories
CREATE TABLE product_categories (
                                    category_id    SERIAL PRIMARY KEY,
                                    category_name  VARCHAR(150) NOT NULL,
                                    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
                                    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Products
CREATE TABLE products (
                          product_id     SERIAL PRIMARY KEY,
                          product_name   VARCHAR(150) NOT NULL,
                          sku            VARCHAR(100) UNIQUE,
                          category_id    INT          REFERENCES product_categories(category_id) ON DELETE SET NULL,
                          price          DECIMAL(10,2),
                          is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
                          created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Warehouses (per company)
CREATE TABLE warehouses (
                            warehouse_id    SERIAL PRIMARY KEY,
                            warehouse_name  VARCHAR(150) NOT NULL,
                            location        VARCHAR(255),
                            company_id      INT NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
                            is_active       BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Inventory (product in warehouse with quantity)
CREATE TABLE inventory_locations (
                                     inventory_id    SERIAL PRIMARY KEY,
                                     product_id      INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
                                     warehouse_id    INT NOT NULL REFERENCES warehouses(warehouse_id) ON DELETE CASCADE,
                                     quantity        INT NOT NULL DEFAULT 0,
                                     last_updated    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     UNIQUE (product_id, warehouse_id)
);

-- Business partners (customers / suppliers)
CREATE TABLE business_partners (
                                   partner_id     SERIAL PRIMARY KEY,
                                   partner_name   VARCHAR(150) NOT NULL,
                                   partner_type   VARCHAR(50)  NOT NULL, -- supplier / customer
                                   phone          VARCHAR(50),
                                   email          VARCHAR(150),
                                   address        VARCHAR(255),
                                   is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
                                   created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bulk imports (file uploads tracking)
CREATE TABLE bulk_imports (
                              import_id      SERIAL PRIMARY KEY,
                              company_id     INT NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
                              entity_type    VARCHAR(100) NOT NULL,
                              file_name      VARCHAR(255),
                              total_rows     INT,
                              success_rows   INT,
                              failed_rows    INT,
                              status         VARCHAR(50),
                              imported_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bulk import errors (row-level issues)
CREATE TABLE bulk_import_errors (
                                    error_id       SERIAL PRIMARY KEY,
                                    import_id      INT NOT NULL REFERENCES bulk_imports(import_id) ON DELETE CASCADE,
                                    row_num        INT,
                                    field_name     VARCHAR(100),
                                    error_message  VARCHAR(255)
);