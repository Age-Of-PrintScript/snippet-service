-- Drop foreign key from snippet_test to allow composite PK on snippet
ALTER TABLE snippet_test DROP CONSTRAINT IF EXISTS snippet_test_snippet_id_fkey;

-- Rename existing language version column to avoid collision
ALTER TABLE snippet RENAME COLUMN version TO language_version;

-- Add snippet version and is_latest columns
ALTER TABLE snippet ADD COLUMN version INT NOT NULL DEFAULT 1;
ALTER TABLE snippet ADD COLUMN is_latest BOOLEAN NOT NULL DEFAULT true;

-- Update primary key to be composite (id, version)
ALTER TABLE snippet DROP CONSTRAINT snippet_pkey;
ALTER TABLE snippet ADD PRIMARY KEY (id, version);

-- Indexes for querying the active/latest version efficiently
CREATE INDEX idx_snippet_id_latest ON snippet(id, is_latest);
CREATE INDEX idx_snippet_owner_latest ON snippet(owner_id, is_latest);
