CREATE TABLE job (
    id UUID PRIMARY KEY,
    snippet_id UUID NOT NULL,
    snippet_version INT NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    result TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (snippet_id, snippet_version) REFERENCES snippet(id, version) ON DELETE CASCADE
);

CREATE INDEX idx_job_snippet ON job(snippet_id, snippet_version);
CREATE INDEX idx_job_user_id ON job(user_id);
