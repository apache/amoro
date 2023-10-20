ALTER TABLE table_runtime ADD format VARCHAR(32)  NOT NULL;
COMMENT ON COLUMN table_runtime.format IS 'Format';

UPDATE table_runtime A
SET format = catalog_properties::json ->>'table-formats'
FROM catalog_metadata B
WHERE A.catalog_name = B.catalog_name;

ALTER TABLE table_identifier ADD format VARCHAR(32)  NOT NULL;
COMMENT ON COLUMN table_identifier.format IS 'Format';

UPDATE table_identifier A
SET format = catalog_properties::json ->>'table-formats'
FROM catalog_metadata B
WHERE A.catalog_name = B.catalog_name;