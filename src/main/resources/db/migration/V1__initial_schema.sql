CREATE TABLE snippet (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    language VARCHAR(50) NOT NULL,
    version VARCHAR(50) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_snippet_owner_id ON snippet(owner_id);

CREATE TABLE snippet_test (
    id UUID PRIMARY KEY,
    snippet_id UUID NOT NULL REFERENCES snippet(id) ON DELETE CASCADE,
    owner_id VARCHAR(128) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_snippet_test_snippet_id ON snippet_test(snippet_id);
