ALTER TABLE table_runtime ADD format VARCHAR(32)  NOT NULL;
COMMENT ON COLUMN table_runtime.format IS 'Format';