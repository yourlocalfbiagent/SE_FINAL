-- Seed the global catalogs that every tenant composes roles from.
INSERT INTO modules (module_name, description) VALUES
    ('MASTER DATA', 'Manage Products, Partners, Warehouses, and Categories'),
    ('SALES',       'Manage Sales Orders, Invoices, Payments, and Returns'),
    ('PURCHASING',  'Manage Purchase Orders, Goods Receipts, and Supplier Bills'),
    ('INVENTORY',   'Manage Stock Levels, Movements, and Counts'),
    ('REPORTS',     'Access Sales and Inventory reports'),
    ('ADMIN',       'Manage Users, Roles, Permissions, and System Config');

INSERT INTO actions (action_name, description) VALUES
    ('create',  'Create new records'),
    ('read',    'View existing records'),
    ('update',  'Modify existing records'),
    ('delete',  'Remove records from the system'),
    ('approve', 'Approve pending transactions or documents'),
    ('export',  'Export data to CSV or PDF');

-- Generate every (module, action) combination as a permission.
INSERT INTO permissions (module_id, action_id)
SELECT m.module_id, a.action_id
FROM modules m CROSS JOIN actions a;
