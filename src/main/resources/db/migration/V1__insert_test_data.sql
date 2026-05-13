-- Seed Data for Testing (Phase 1 & 2)

-- 1. Users (1 Creator, 1 Reviewer)
INSERT INTO USERS (first_name, last_name, email, password_hash, is_active) 
VALUES 
('Alice', 'Creator', 'alice@erp.com', '$2a$10$abcdef', true),
('Bob', 'Reviewer', 'bob@erp.com', '$2a$10$abcdef', true);

-- 2. Business Partners (1 Customer)
INSERT INTO BUSINESS_PARTNERS (partner_name, email, phone, type, is_active)
VALUES 
('Global Tech Industries', 'contact@globaltech.com', '+1-555-0123', 'CUSTOMER', true);

-- 3. Products (3 Items with different prices)
INSERT INTO PRODUCTS (product_name, sku, unit_of_measure, cost_price, selling_price, reorder_level)
VALUES 
('Pro Laptop Z1', 'SKU-LAP-001', 'UNIT', 800.00, 1200.00, 5.0),
('Wireless Mouse M2', 'SKU-MOU-002', 'UNIT', 15.00, 45.00, 20.0),
('Mechanical Keyboard K3', 'SKU-KBD-003', 'UNIT', 40.00, 85.00, 10.0);

-- 4. Sample Sales Order (DRAFT)
INSERT INTO SALES_ORDERS (sales_order_number, partner_id, created_by, order_date, subtotal, tax_amount, total_amount, status)
VALUES 
('SO-TEST-001', 1, 1, CURRENT_DATE, 1245.00, 124.50, 1369.50, 'DRAFT');

INSERT INTO SALES_ORDER_LINES (sales_order_id, product_id, quantity, unit_price, line_total)
VALUES 
(1, 1, 1, 1200.00, 1200.00),
(1, 2, 1, 45.00, 45.00);

-- 5. Sample Sales Invoice (UNPAID)
INSERT INTO SALES_INVOICES (invoice_number, sales_order_id, partner_id, created_by, invoice_date, due_date, subtotal, tax_amount, total_amount, status)
VALUES 
('INV-TEST-001', 1, 1, 1, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 1245.00, 124.50, 1369.50, 'UNPAID');

INSERT INTO SALES_INVOICE_LINES (invoice_id, product_id, quantity, unit_price, line_total)
VALUES 
(1, 1, 1, 1200.00, 1200.00),
(1, 2, 1, 45.00, 45.00);
