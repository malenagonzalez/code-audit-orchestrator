ALTER TABLE audit_records ADD COLUMN pedagogical_explanation TEXT;
ALTER TABLE audit_records ADD COLUMN completed_at TEXT;

CREATE TABLE audit_issues (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    audit_record_id  INTEGER NOT NULL REFERENCES audit_records(id) ON DELETE CASCADE,
    severity         TEXT,
    category         TEXT,
    title            TEXT,
    description      TEXT,
    line_start       INTEGER,
    line_end         INTEGER,
    refactored_code  TEXT
);
