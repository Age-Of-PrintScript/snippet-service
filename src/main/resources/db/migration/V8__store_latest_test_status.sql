ALTER TABLE snippet_test
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'NOT_RUN',
    ADD COLUMN active_job_id UUID REFERENCES job(id) ON DELETE SET NULL;

CREATE INDEX idx_snippet_test_active_job ON snippet_test(active_job_id);

DROP TABLE test_execution;
DROP TABLE test_run;
