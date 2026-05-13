-- FR-18 / NFR-12: every update/approve/delete on critical entities records an audit entry.
CREATE TABLE audit_log (
    audit_id    BIGSERIAL PRIMARY KEY,
    user_id     INT REFERENCES users(user_id) ON DELETE SET NULL,    -- NULL = system action (e.g. seed)
    company_id  INT NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    action      VARCHAR(50)  NOT NULL,                               -- e.g. "user.create", "company.update"
    entity_type VARCHAR(50)  NOT NULL,                               -- "user", "role", "company", "permission"
    entity_id   INT,                                                 -- nullable when action targets no single record
    details     TEXT,                                                -- free-form note
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_company_time ON audit_log (company_id, created_at DESC);
CREATE INDEX idx_audit_user         ON audit_log (user_id);
