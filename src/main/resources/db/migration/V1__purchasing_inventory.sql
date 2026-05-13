-- Purchasing & Inventory Module
-- Flyway migration

CREATE TABLE purchase_orders (
    po_id SERIAL PRIMARY KEY,
    po_number VARCHAR(50) UNIQUE NOT NULL,
    partner_id INT NOT NULL,
    created_by INT NOT NULL,
    order_date DATE NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_po_total CHECK (total_amount >= 0),
    CONSTRAINT chk_po_status CHECK (status IN ('pending', 'approved', 'rejected', 'completed', 'cancelled'))
);

CREATE TABLE purchase_order_lines (
    line_id SERIAL PRIMARY KEY,
    po_id INT NOT NULL REFERENCES purchase_orders(po_id) ON DELETE CASCADE,
    product_id INT NOT NULL,
    quantity_ordered NUMERIC(12,2) NOT NULL,
    unit_cost NUMERIC(12,2) NOT NULL,
    line_total NUMERIC(12,2) NOT NULL,

    CONSTRAINT chk_po_line_qty CHECK (quantity_ordered > 0),
    CONSTRAINT chk_po_line_unit_cost CHECK (unit_cost >= 0),
    CONSTRAINT chk_po_line_total CHECK (line_total >= 0)
);

CREATE TABLE supplier_bills (
    bill_id SERIAL PRIMARY KEY,
    bill_number VARCHAR(50) UNIQUE NOT NULL,
    partner_id INT NOT NULL,
    po_id INT REFERENCES purchase_orders(po_id),
    recorded_by INT NOT NULL,
    bill_date DATE NOT NULL,
    due_date DATE,
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'draft',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_bill_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_bill_tax CHECK (tax_amount >= 0),
    CONSTRAINT chk_bill_total CHECK (total_amount >= 0),
    CONSTRAINT chk_bill_dates CHECK (due_date IS NULL OR due_date >= bill_date),
    CONSTRAINT chk_bill_status CHECK (status IN ('draft', 'issued', 'partially_paid', 'paid', 'cancelled'))
);

CREATE TABLE supplier_bill_lines (
    bill_line_id SERIAL PRIMARY KEY,
    bill_id INT NOT NULL REFERENCES supplier_bills(bill_id) ON DELETE CASCADE,
    product_id INT NOT NULL,
    quantity NUMERIC(12,2) NOT NULL,
    unit_cost NUMERIC(12,2) NOT NULL,
    line_total NUMERIC(12,2) NOT NULL,

    CONSTRAINT chk_supplier_bill_line_qty CHECK (quantity > 0),
    CONSTRAINT chk_supplier_bill_line_unit_cost CHECK (unit_cost >= 0),
    CONSTRAINT chk_supplier_bill_line_total CHECK (line_total >= 0)
);

CREATE TABLE goods_receipts (
    receipt_id SERIAL PRIMARY KEY,
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    po_id INT NOT NULL REFERENCES purchase_orders(po_id),
    received_by INT NOT NULL,
    receipt_date DATE NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE goods_receipt_lines (
    receipt_line_id SERIAL PRIMARY KEY,
    receipt_id INT NOT NULL REFERENCES goods_receipts(receipt_id) ON DELETE CASCADE,
    product_id INT NOT NULL,
    quantity_received NUMERIC(12,2) NOT NULL,

    CONSTRAINT chk_goods_receipt_qty CHECK (quantity_received > 0)
);

CREATE TABLE inventory_counts (
    count_id SERIAL PRIMARY KEY,
    count_number VARCHAR(50) UNIQUE NOT NULL,
    warehouse_id INT NOT NULL,
    counted_by INT NOT NULL,
    count_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'draft',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_inventory_count_status CHECK (status IN ('draft', 'in_progress', 'completed', 'cancelled'))
);

CREATE TABLE inventory_count_lines (
    count_line_id SERIAL PRIMARY KEY,
    count_id INT NOT NULL REFERENCES inventory_counts(count_id) ON DELETE CASCADE,
    product_id INT NOT NULL,
    system_quantity NUMERIC(12,2) NOT NULL,
    counted_quantity NUMERIC(12,2) NOT NULL,
    variance_quantity NUMERIC(12,2) NOT NULL,

    CONSTRAINT chk_inventory_system_qty CHECK (system_quantity >= 0),
    CONSTRAINT chk_inventory_counted_qty CHECK (counted_quantity >= 0)
);

CREATE TABLE inventory_discrepancies (
    discrepancy_id SERIAL PRIMARY KEY,
    count_line_id INT NOT NULL REFERENCES inventory_count_lines(count_line_id) ON DELETE CASCADE,
    recorded_by INT NOT NULL,
    resolved_by INT,
    system_quantity NUMERIC(12,2) NOT NULL,
    counted_quantity NUMERIC(12,2) NOT NULL,
    variance_quantity NUMERIC(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'open',
    notes VARCHAR(255),
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,

    CONSTRAINT chk_discrepancy_system_qty CHECK (system_quantity >= 0),
    CONSTRAINT chk_discrepancy_counted_qty CHECK (counted_quantity >= 0),
    CONSTRAINT chk_discrepancy_status CHECK (status IN ('open', 'resolved', 'ignored')),
    CONSTRAINT chk_discrepancy_resolved_time CHECK (resolved_at IS NULL OR resolved_at >= recorded_at)
);

CREATE TABLE stock_movements (
    movement_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    location_id INT NOT NULL,
    goods_receipt_line_id INT REFERENCES goods_receipt_lines(receipt_line_id),
    discrepancy_id INT REFERENCES inventory_discrepancies(discrepancy_id),
    quantity_change NUMERIC(12,2) NOT NULL,
    reason_code VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50),
    reference_id INT,
    moved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_stock_quantity_change CHECK (quantity_change <> 0),
    CONSTRAINT chk_stock_reference CHECK (
        goods_receipt_line_id IS NOT NULL
        OR discrepancy_id IS NOT NULL
        OR reference_id IS NOT NULL
    )
);

-- Indexes for performance and future FK columns

CREATE INDEX idx_purchase_orders_partner_id ON purchase_orders(partner_id);
CREATE INDEX idx_purchase_orders_created_by ON purchase_orders(created_by);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(status);

CREATE INDEX idx_purchase_order_lines_po_id ON purchase_order_lines(po_id);
CREATE INDEX idx_purchase_order_lines_product_id ON purchase_order_lines(product_id);

CREATE INDEX idx_supplier_bills_partner_id ON supplier_bills(partner_id);
CREATE INDEX idx_supplier_bills_po_id ON supplier_bills(po_id);
CREATE INDEX idx_supplier_bills_status ON supplier_bills(status);

CREATE INDEX idx_supplier_bill_lines_bill_id ON supplier_bill_lines(bill_id);
CREATE INDEX idx_supplier_bill_lines_product_id ON supplier_bill_lines(product_id);

CREATE INDEX idx_goods_receipts_po_id ON goods_receipts(po_id);
CREATE INDEX idx_goods_receipts_received_by ON goods_receipts(received_by);

CREATE INDEX idx_goods_receipt_lines_receipt_id ON goods_receipt_lines(receipt_id);
CREATE INDEX idx_goods_receipt_lines_product_id ON goods_receipt_lines(product_id);

CREATE INDEX idx_inventory_counts_warehouse_id ON inventory_counts(warehouse_id);
CREATE INDEX idx_inventory_counts_counted_by ON inventory_counts(counted_by);
CREATE INDEX idx_inventory_counts_status ON inventory_counts(status);

CREATE INDEX idx_inventory_count_lines_count_id ON inventory_count_lines(count_id);
CREATE INDEX idx_inventory_count_lines_product_id ON inventory_count_lines(product_id);

CREATE INDEX idx_inventory_discrepancies_count_line_id ON inventory_discrepancies(count_line_id);
CREATE INDEX idx_inventory_discrepancies_status ON inventory_discrepancies(status);

CREATE INDEX idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_location_id ON stock_movements(location_id);
CREATE INDEX idx_stock_movements_reason_code ON stock_movements(reason_code);