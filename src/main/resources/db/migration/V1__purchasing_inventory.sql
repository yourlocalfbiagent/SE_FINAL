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


ALTER TABLE purchase_order_lines
ADD CONSTRAINT chk_po_line_qty CHECK (quantity_ordered > 0),
ADD CONSTRAINT chk_po_line_unit_cost CHECK (unit_cost >= 0),
ADD CONSTRAINT chk_po_line_total CHECK (line_total >= 0);

CREATE INDEX idx_purchase_orders_partner_id ON purchase_orders(partner_id);
CREATE INDEX idx_purchase_orders_created_by ON purchase_orders(created_by);
CREATE INDEX idx_purchase_order_lines_po_id ON purchase_order_lines(po_id);
CREATE INDEX idx_purchase_order_lines_product_id ON purchase_order_lines(product_id);


ALTER TABLE supplier_bill_lines
ADD CONSTRAINT chk_supplier_bill_line_qty CHECK (quantity > 0),
ADD CONSTRAINT chk_supplier_bill_line_unit_cost CHECK (unit_cost >= 0),
ADD CONSTRAINT chk_supplier_bill_line_total CHECK (line_total >= 0);

ALTER TABLE goods_receipt_lines
ADD CONSTRAINT chk_goods_receipt_qty CHECK (quantity_received > 0);

ALTER TABLE inventory_count_lines
ADD CONSTRAINT chk_inventory_system_qty CHECK (system_quantity >= 0),
ADD CONSTRAINT chk_inventory_counted_qty CHECK (counted_quantity >= 0);

ALTER TABLE stock_movements
ADD CONSTRAINT chk_stock_quantity_change CHECK (quantity_change <> 0);

CREATE INDEX idx_supplier_bills_partner_id ON supplier_bills(partner_id);
CREATE INDEX idx_supplier_bills_po_id ON supplier_bills(po_id);
CREATE INDEX idx_supplier_bill_lines_bill_id ON supplier_bill_lines(bill_id);
CREATE INDEX idx_goods_receipts_po_id ON goods_receipts(po_id);
CREATE INDEX idx_goods_receipt_lines_receipt_id ON goods_receipt_lines(receipt_id);
CREATE INDEX idx_inventory_counts_warehouse_id ON inventory_counts(warehouse_id);
CREATE INDEX idx_inventory_count_lines_count_id ON inventory_count_lines(count_id);
CREATE INDEX idx_inventory_discrepancies_count_line_id ON inventory_discrepancies(count_line_id);
CREATE INDEX idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_location_id ON stock_movements(location_id);