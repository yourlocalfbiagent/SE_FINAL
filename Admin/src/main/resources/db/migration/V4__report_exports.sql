-- FR-19 / FR-20: report generation and export tracking.
CREATE TABLE report_exports (
    export_id    SERIAL      PRIMARY KEY,
    company_id   INT         NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    generated_by INT         REFERENCES users(user_id) ON DELETE SET NULL,
    report_type  VARCHAR(50) NOT NULL,   -- e.g. 'audit-log', 'admin-stats'
    period_start DATE,
    period_end   DATE,
    file_format  VARCHAR(10) NOT NULL DEFAULT 'csv',
    row_count    INT,
    generated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_report_exports_company ON report_exports (company_id, generated_at DESC);
