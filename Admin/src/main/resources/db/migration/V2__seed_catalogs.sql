-- Seed the global catalogs that every tenant composes roles from.
INSERT INTO modules (module_name) VALUES
    ('admin'),
    ('users'),
    ('roles'),
    ('companies'),
    ('inventory'),
    ('sales'),
    ('purchases'),
    ('finance'),
    ('reports');

INSERT INTO actions (action_name) VALUES
    ('create'),
    ('read'),
    ('update'),
    ('delete'),
    ('approve'),
    ('export');

-- Generate every (module, action) combination as a permission.
INSERT INTO permissions (module_id, action_id)
SELECT m.module_id, a.action_id
FROM modules m CROSS JOIN actions a;
