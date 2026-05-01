CREATE TABLE purchase_orders (
    po_id SERIAL PRIMARY KEY,
    po_number VARCHAR(50) UNIQUE NOT NULL,
    partner_id INT NOT NULL,
    created_by INT NOT NULL,
    order_date DATE NOT NULL,
    total_amount DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE purchase_order_lines (
    line_id SERIAL PRIMARY KEY,
    po_id INT NOT NULL REFERENCES purchase_orders(po_id),
    product_id INT NOT NULL,
    quantity_ordered DECIMAL(10,2) NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL
);

CREATE TABLE supplier_bills (
    bill_id SERIAL PRIMARY KEY,
    bill_number VARCHAR(50) UNIQUE NOT NULL,
    partner_id INT NOT NULL,
    po_id INT REFERENCES purchase_orders(po_id),
    recorded_by INT NOT NULL,
    bill_date DATE NOT NULL,
    due_date DATE,
    subtotal DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE supplier_bill_lines (
    bill_line_id SERIAL PRIMARY KEY,
    bill_id INT NOT NULL REFERENCES supplier_bills(bill_id),
    product_id INT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL
);

CREATE TABLE goods_receipts (
    receipt_id SERIAL PRIMARY KEY,
    po_id INT REFERENCES purchase_orders(po_id),
    received_by INT NOT NULL,
    receipt_date DATE NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE goods_receipt_lines (
    receipt_line_id SERIAL PRIMARY KEY,
    receipt_id INT NOT NULL REFERENCES goods_receipts(receipt_id),
    product_id INT NOT NULL,
    quantity_received DECIMAL(10,2) NOT NULL
);

CREATE TABLE inventory_counts (
    count_id SERIAL PRIMARY KEY,
    count_number VARCHAR(50) UNIQUE NOT NULL,
    warehouse_id INT NOT NULL,
    counted_by INT NOT NULL,
    count_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_count_lines (
    count_line_id SERIAL PRIMARY KEY,
    count_id INT NOT NULL REFERENCES inventory_counts(count_id),
    product_id INT NOT NULL,
    system_quantity DECIMAL(10,2) NOT NULL,
    counted_quantity DECIMAL(10,2) NOT NULL,
    variance_quantity DECIMAL(10,2) NOT NULL
);

CREATE TABLE inventory_discrepancies (
    discrepancy_id SERIAL PRIMARY KEY,
    count_line_id INT NOT NULL REFERENCES inventory_count_lines(count_line_id),
    recorded_by INT NOT NULL,
    resolved_by INT,
    system_quantity DECIMAL(10,2) NOT NULL,
    counted_quantity DECIMAL(10,2) NOT NULL,
    variance_quantity DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(255),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE TABLE stock_movements (
    movement_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    location_id INT NOT NULL,
    goods_receipt_line_id INT REFERENCES goods_receipt_lines(receipt_line_id),
    discrepancy_id INT REFERENCES inventory_discrepancies(discrepancy_id),
    quantity_change DECIMAL(10,2) NOT NULL,
    reason_code VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50),
    reference_id INT,
    moved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);