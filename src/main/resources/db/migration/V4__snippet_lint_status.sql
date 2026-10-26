CREATE TABLE snippet_lint_status (
    snippet_id UUID NOT NULL,
    version INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    warnings TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (snippet_id, version),
    FOREIGN KEY (snippet_id, version) REFERENCES snippet(id, version) ON DELETE CASCADE
);
