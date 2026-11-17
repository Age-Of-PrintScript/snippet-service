CREATE TABLE test_run (
    id UUID PRIMARY KEY,
    snippet_id UUID NOT NULL,
    snippet_version INT NOT NULL,
    requested_by VARCHAR(128) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    FOREIGN KEY (snippet_id, snippet_version) REFERENCES snippet(id, version) ON DELETE CASCADE
);

CREATE INDEX idx_test_run_snippet ON test_run(snippet_id, snippet_version);
CREATE INDEX idx_test_run_requested_by ON test_run(requested_by);

CREATE TABLE test_execution (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES test_run(id) ON DELETE CASCADE,
    test_id UUID NOT NULL REFERENCES snippet_test(id) ON DELETE CASCADE,
    job_id UUID NOT NULL UNIQUE REFERENCES job(id) ON DELETE CASCADE,
    test_url VARCHAR(1024) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ
);

CREATE INDEX idx_test_execution_run ON test_execution(run_id);
CREATE INDEX idx_test_execution_test ON test_execution(test_id);
