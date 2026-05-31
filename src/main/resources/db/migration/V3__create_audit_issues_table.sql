CREATE TABLE IF NOT EXISTS audit_issues (
    id               TEXT PRIMARY KEY,
    audit_record_id  TEXT NOT NULL REFERENCES audit_records(id) ON DELETE CASCADE,
    severity         TEXT NOT NULL,
    category         TEXT NOT NULL,
    title            TEXT NOT NULL,
    description      TEXT NOT NULL,
    line_start       INTEGER,
    line_end         INTEGER,
    refactored_code  TEXT
);

CREATE INDEX IF NOT EXISTS idx_audit_issues_record_id ON audit_issues(audit_record_id);
