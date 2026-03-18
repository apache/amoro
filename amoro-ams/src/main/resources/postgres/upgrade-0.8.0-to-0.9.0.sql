ALTER TABLE table_metadata
    ADD COLUMN table_schema TEXT,
    ADD COLUMN bucket_mode VARCHAR(64),
    ADD COLUMN num_buckets INT,
    ADD COLUMN create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN snapshot_id VARCHAR(64),
    ADD COLUMN table_comment VARCHAR(512);

COMMENT ON COLUMN table_metadata.table_schema IS 'Table schema json';
COMMENT ON COLUMN table_metadata.bucket_mode IS 'Paimon bucket mode';
COMMENT ON COLUMN table_metadata.num_buckets IS 'Paimon bucket count';
COMMENT ON COLUMN table_metadata.create_time IS 'Create time';
COMMENT ON COLUMN table_metadata.update_time IS 'Update time';
COMMENT ON COLUMN table_metadata.snapshot_id IS 'Latest snapshot id';
COMMENT ON COLUMN table_metadata.table_comment IS 'Table comment';
