CREATE TABLE audit_records (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id    INTEGER NOT NULL REFERENCES users(id),
    code       TEXT,
    language   TEXT,
    status     TEXT,
    created_at TEXT
);
