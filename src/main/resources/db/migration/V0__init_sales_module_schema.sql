-- Referenced Tables (3) - Complete structure without external FK constraints
CREATE TABLE USERS (
    user_id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    company_id INT, -- Reference to external COMPANIES
    role_id INT,    -- Reference to external ROLES
    is_active BOOLEAN DEFAULT TRUE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE BUSINESS_PARTNERS (
    partner_id SERIAL PRIMARY KEY,
    partner_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    type VARCHAR(50), 
    company_id INT, -- Reference to external COMPANIES
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE PRODUCTS (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    unit_of_measure VARCHAR(50),
    cost_price DECIMAL(19,4) NOT NULL,
    selling_price DECIMAL(19,4) NOT NULL,
    reorder_level DECIMAL(19,4) DEFAULT 0,
    category_id INT, -- Reference to external PRODUCT_CATEGORIES
    company_id INT,  -- Reference to external COMPANIES
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sales Module Tables (8)
CREATE TABLE SALES_ORDERS (
    sales_order_id SERIAL PRIMARY KEY,
    sales_order_number VARCHAR(100) UNIQUE NOT NULL,
    partner_id INT REFERENCES BUSINESS_PARTNERS(partner_id),
    created_by INT REFERENCES USERS(user_id),
    order_date DATE DEFAULT CURRENT_DATE,
    subtotal DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE SALES_ORDER_LINES (
    line_id SERIAL PRIMARY KEY,
    sales_order_id INT REFERENCES SALES_ORDERS(sales_order_id),
    product_id INT REFERENCES PRODUCTS(product_id),
    quantity DECIMAL(19,4) NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    line_total DECIMAL(19,4) NOT NULL
);

CREATE TABLE SALES_INVOICES (
    invoice_id SERIAL PRIMARY KEY,
    invoice_number VARCHAR(100) UNIQUE NOT NULL,
    sales_order_id INT REFERENCES SALES_ORDERS(sales_order_id),
    partner_id INT REFERENCES BUSINESS_PARTNERS(partner_id),
    created_by INT REFERENCES USERS(user_id),
    invoice_date DATE DEFAULT CURRENT_DATE,
    due_date DATE,
    subtotal DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE SALES_INVOICE_LINES (
    line_id SERIAL PRIMARY KEY,
    invoice_id INT REFERENCES SALES_INVOICES(invoice_id),
    product_id INT REFERENCES PRODUCTS(product_id),
    quantity DECIMAL(19,4) NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    line_total DECIMAL(19,4) NOT NULL
);

CREATE TABLE PAYMENTS (
    payment_id SERIAL PRIMARY KEY,
    invoice_id INT REFERENCES SALES_INVOICES(invoice_id),
    amount DECIMAL(19,4) NOT NULL,
    payment_date DATE DEFAULT CURRENT_DATE,
    payment_method VARCHAR(50),
    reference VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE APPROVAL_REQUESTS (
    approval_id SERIAL PRIMARY KEY,
    invoice_id INT REFERENCES SALES_INVOICES(invoice_id),
    requested_by INT REFERENCES USERS(user_id),
    reviewed_by INT REFERENCES USERS(user_id),
    status VARCHAR(50) DEFAULT 'PENDING',
    comments TEXT,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP
);

CREATE TABLE SALES_RETURNS (
    return_id SERIAL PRIMARY KEY,
    return_number VARCHAR(100) UNIQUE NOT NULL,
    invoice_id INT REFERENCES SALES_INVOICES(invoice_id),
    processed_by INT REFERENCES USERS(user_id),
    return_date DATE DEFAULT CURRENT_DATE,
    reason TEXT,
    total_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE SALES_RETURN_LINES (
    return_line_id SERIAL PRIMARY KEY,
    return_id INT REFERENCES SALES_RETURNS(return_id),
    product_id INT REFERENCES PRODUCTS(product_id),
    quantity DECIMAL(19,4) NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    line_total DECIMAL(19,4) NOT NULL
);
