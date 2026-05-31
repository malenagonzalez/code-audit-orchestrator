CREATE TABLE IF NOT EXISTS audit_records (
    id                       TEXT PRIMARY KEY,
    user_id                  TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code_snippet             TEXT NOT NULL,
    language                 TEXT NOT NULL,
    status                   TEXT NOT NULL DEFAULT 'PENDING',
    pedagogical_explanation  TEXT,
    created_at               TEXT NOT NULL DEFAULT (datetime('now')),
    completed_at             TEXT
);

CREATE INDEX IF NOT EXISTS idx_audit_records_user_id    ON audit_records(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_records_created_at ON audit_records(created_at DESC);
