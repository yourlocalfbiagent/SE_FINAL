INSERT INTO business_partners (partner_name, email, phone, address, city, country, type, company_id, is_active, created_at)
VALUES
  ('Acme Corp', 'contact@acme.com', '+1-555-0101', '123 Main St', 'New York', 'USA', 'CUSTOMER', 1, true, NOW()),
  ('Global Supplies Ltd', 'sales@globalsupplies.com', '+1-555-0202', '456 Oak Ave', 'Chicago', 'USA', 'SUPPLIER', 1, true, NOW()),
  ('Tech Solutions Inc', 'info@techsolutions.com', '+1-555-0303', '789 Pine Rd', 'San Francisco', 'USA', 'CUSTOMER', 1, true, NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO products (product_name, sku, unit_of_measure, cost_price, selling_price, reorder_level, company_id, is_active, created_at)
VALUES
  ('Laptop 15"', 'LAP-001', 'UNIT', 800.00, 1200.00, 5, 1, true, NOW()),
  ('Wireless Mouse', 'MOU-001', 'UNIT', 15.00, 35.00, 20, 1, true, NOW()),
  ('USB-C Hub', 'HUB-001', 'UNIT', 25.00, 60.00, 15, 1, true, NOW()),
  ('Office Chair', 'CHR-001', 'UNIT', 150.00, 350.00, 3, 1, true, NOW())
ON CONFLICT (sku) DO NOTHING;
